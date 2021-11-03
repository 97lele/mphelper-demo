package com.xl.mphelper.shard;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xl.mphelper.annonations.TableShardIgnore;
import com.xl.mphelper.util.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import com.xl.mphelper.annonations.TableShard;
import com.xl.mphelper.annonations.TableShardParam;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author tanjl11
 * @date 2021/10/15 15:34
 * 默认不加载这个bean
 */
@Slf4j
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
@Component
@ConditionalOnExpression("${mphelper.shard-support:false}")
public class TableShardInterceptor implements Interceptor {
    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory REFLECTOR_FACTORY = new DefaultReflectorFactory();

    private static final Map<String, Class> MAPPER_CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class, ITableShardStrategy> SHARD_STRATEGY = new ConcurrentHashMap<>();
    private static final Map<Class, ITableShardDbType> SHARD_DB = new ConcurrentHashMap<>();

    private static final Set<String> HANDLED_TABLE = new ConcurrentSkipListSet<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //入口处进行处理
        if (!(invocation.getTarget() instanceof RoutingStatementHandler)) {
            return invocation.proceed();
        }
        if (TableShardHolder.isIgnore()) {
            return invocation.proceed();
        }
        RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation.getTarget();
        //获取
        MetaObject metaObject = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        Class<? extends BaseMapper> mapperClass = getMapperClass(mappedStatement);
        if (!mapperClass.isAnnotationPresent(TableShard.class)) {
            return invocation.proceed();
        }
        TableShard annotation = mapperClass.getAnnotation(TableShard.class);
        ExecBaseMethod.MethodInfo curMethod = getExecMethod(mappedStatement, mapperClass, annotation);
        if (curMethod.shouldIgnore) {
            return invocation.proceed();
        }
        Set<String> tableNames = getTableNames(boundSql, annotation);
        Map<String, String> routingTableMap = new HashMap<>(tableNames.size());
        if (TableShardHolder.hasVal()) {
            for (String tableName : tableNames) {
                if (TableShardHolder.containTable(tableName)) {
                    routingTableMap.put(tableName, TableShardHolder.getReplaceName(tableName));
                }
            }
        }
        if (annotation.enableCreateTable()) {
            //默认是用执行的mapper进行表新建
            handleTableCreate(invocation, metaObject, mapperClass, routingTableMap, annotation);
        }
        if(TableShardHolder.hasVal()){
            replaceSql(metaObject, boundSql, routingTableMap);
            return invocation.proceed();
        }
        Class<? extends ITableShardStrategy> shardStrategy = annotation.shardStrategy();
        ITableShardStrategy strategy = SHARD_STRATEGY.computeIfAbsent(shardStrategy, e -> (ITableShardStrategy) getObjectByClass(e));
        if (strategy == null) {
            return invocation.proceed();
        }
        Object objFromCurMethod = null;
        for (String tableName : tableNames) {
            String resName = null;
            if (objFromCurMethod == null) {
                Pair<Object, ITableShardStrategy> res = getObjFromCurMethod(curMethod.parameters, boundSql);
                if (res.getRight() != null) {
                    strategy = res.getRight();
                }
                objFromCurMethod = res.getLeft();
            }
            resName = strategy.routingTable(tableName, objFromCurMethod);
            routingTableMap.put(tableName, resName);
        }
        //处理表sql
        replaceSql(metaObject, boundSql, routingTableMap);
        return invocation.proceed();
    }

    private void replaceSql(MetaObject metaObject, BoundSql boundSql, Map<String, String> routingTableMap) {
        String sql = boundSql.getSql();
        for (Map.Entry<String, String> entry : routingTableMap.entrySet()) {
            sql = sql.replaceAll(entry.getKey(), entry.getValue());
        }
        metaObject.setValue("delegate.boundSql.sql", sql);
    }

    private void handleTableCreate(Invocation invocation, MetaObject metaStatementHandler, Class<? extends BaseMapper> mapperClass, Map<String, String> routingTableMap, TableShard annotation) throws SQLException {
        //代表已经处理了这些表
        boolean exec = false;
        Collection<String> curTableValues = routingTableMap.values();
        for (String value : curTableValues) {
            if (!HANDLED_TABLE.contains(value)) {
                exec = true;
                break;
            }
        }
        if (!exec) {
            return;
        }
        String tableMethod = annotation.createTableMethod();
        Method createTableMethod = null;
        if (tableMethod.length() > 0) {
            createTableMethod = ReflectionUtils.findMethod(mapperClass, tableMethod);
        }
        if (createTableMethod != null && !createTableMethod.isAnnotationPresent(TableShardIgnore.class)) {
            SqlSessionTemplate template = ApplicationContextHolder.getBean(SqlSessionTemplate.class);
            String methodPath = mapperClass.getName() + "." + tableMethod;
            Configuration configuration = template.getConfiguration();
            String createTableSql = configuration.getMappedStatement(methodPath).getBoundSql("delegate.boundSql").getSql();
            //判断是否已经有这个表
            Set<String> prepareHandledTable = new HashSet<>();
            for (Map.Entry<String, String> entry : routingTableMap.entrySet()) {
                if (createTableSql.contains(entry.getKey())) {
                    prepareHandledTable.add(entry.getKey());
                    createTableSql = createTableSql.replaceAll(entry.getKey(), entry.getValue());
                }
            }
            Connection conn = (Connection) invocation.getArgs()[0];
            boolean preAutoCommitState = conn.getAutoCommit();
            conn.setAutoCommit(false);
            Class<? extends ITableShardDbType> shardDb = annotation.dbType();
            ITableShardDbType iTableShardDb = SHARD_DB.computeIfAbsent(shardDb, e -> (ITableShardDbType) getObjectByClass(shardDb));
            //如果没有检查sql,默认已经建表
            String checkTableSQL = iTableShardDb.getCheckTableSQL(curTableValues);
            boolean contains = existsTable(conn, curTableValues, checkTableSQL);
            if (contains) {
                conn.setAutoCommit(preAutoCommitState);
                HANDLED_TABLE.addAll(prepareHandledTable);
                return;
            }
            try (PreparedStatement countStmt = conn.prepareStatement(createTableSql)) {
                countStmt.execute();
                conn.commit();//执行完后，手动提交事务
            } catch (Exception e) {
                log.error("自动建表报错", e);
            } finally {
                //恢复状态
                conn.setAutoCommit(preAutoCommitState);
                HANDLED_TABLE.addAll(prepareHandledTable);
            }
        }
    }


    private boolean existsTable(Connection conn, Collection<String> tables, String checkTableSql) {
        if (StringUtils.isEmpty(checkTableSql)) {
            return true;
        }

        try (PreparedStatement checkTableExists = conn.prepareStatement(checkTableSql);) {
            boolean preAutoCommitState = conn.getAutoCommit();
            conn.setAutoCommit(false);
            ResultSet resultSet = checkTableExists.executeQuery();
            conn.setAutoCommit(preAutoCommitState);
            //获取已有的表名,判断是否都已经有了这个表
            boolean hasAllTable = true;
            Set<String> resSet = new HashSet<>();
            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                resSet.add(tableName);
            }
            Iterator<String> iterator = tables.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                hasAllTable = hasAllTable && resSet.contains(next);
            }
            //表明此次分表都是有的，不走下面逻辑
            return hasAllTable;
        } catch (SQLException e) {
            log.error("自动建表报错", e);
        }
        return false;
    }

    private Class<? extends BaseMapper> getMapperClass(MappedStatement mappedStatement) {
        String id = mappedStatement.getId();
        //mapperClass
        String className = id.substring(0, id.lastIndexOf("."));
        return MAPPER_CLASS_CACHE.computeIfAbsent(className, name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private ExecBaseMethod.MethodInfo getExecMethod(MappedStatement mappedStatement, Class mapperClass, TableShard annotation) {
        String id = mappedStatement.getId();
        //methodName
        String methodName = id.substring(id.lastIndexOf(".") + 1);
        final Method[] methods = mapperClass.getMethods();
        ExecBaseMethod execMethod = (ExecBaseMethod) getObjectByClass(annotation.execMethodStrategy());
        return execMethod.genMethodInfo(methods, methodName);
    }

    private Set<String> getTableNames(BoundSql boundSql, TableShard shard) {
        Class<? extends ITableShardDbType> shardDb = shard.dbType();
        ITableShardDbType iTableShardDb = SHARD_DB.computeIfAbsent(shardDb, e -> (ITableShardDbType) getObjectByClass(shardDb));
        //获取sql语句
        String originSql = boundSql.getSql();
        DbType dbType = iTableShardDb.getDbType();
        SchemaStatVisitor visitor = SQLUtils.createSchemaStatVisitor(dbType);
        List<SQLStatement> stmtList = SQLUtils.parseStatements(originSql, dbType);
        Set<String> tableNames = new HashSet<>();
        for (int i = 0; i < stmtList.size(); i++) {
            SQLStatement stmt = stmtList.get(i);
            stmt.accept(visitor);
            Map<TableStat.Name, TableStat> tables = visitor.getTables();
            for (TableStat.Name name : tables.keySet()) {
                tableNames.add(name.getName());
            }
        }
        return tableNames;
    }

    /**
     * 如果只有一个参数，取那个
     * 如果有多个，取带SpecificShardTableParam
     *
     * @return
     */
    private Pair<Object, ITableShardStrategy> getObjFromCurMethod(Parameter[] parameters, BoundSql boundSql) {
        Object parameterObject = boundSql.getAdditionalParameter("_parameter");
        if (parameterObject == null) {
            parameterObject = boundSql.getParameterObject();
        }
        if (parameterObject == null || parameters.length == 0) {
            return null;
        }
        Parameter defaultParam = parameters[0];
        ITableShardStrategy res = null;
        for (int i = 0; i < parameters.length; i++) {
            Parameter cur = parameters[i];
            if (cur.isAnnotationPresent(TableShardParam.class)) {
                defaultParam = cur;
                TableShardParam annotation = cur.getAnnotation(TableShardParam.class);
                Class<? extends ITableShardStrategy> shardStrategy = annotation.shardStrategy();
                res = SHARD_STRATEGY.computeIfAbsent(shardStrategy, e -> (ITableShardStrategy) getObjectByClass(e));
                break;
            }
        }
        Object paramValue = null;
        if (defaultParam.isAnnotationPresent(Param.class)) {
            String value = defaultParam.getAnnotation(Param.class).value();
            paramValue = ((MapperMethod.ParamMap) parameterObject).get(value);
        } else {
            paramValue = parameterObject;
        }
        return Pair.of(getInnerObj(paramValue), res);
    }

    private static Object getInnerObj(Object paramValue) {
        if (paramValue instanceof Iterable) {
            Iterable value = (Iterable) paramValue;
            Iterator iterator = value.iterator();
            while (iterator.hasNext()) {
                return getInnerObj(iterator.next());
            }
        }
        return paramValue;
    }


    private Object getObjectByClass(Class<?> invokeClass) {
        return ApplicationContextHolder.getBeanOrInstance(invokeClass);
    }


}
