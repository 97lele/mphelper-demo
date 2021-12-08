package com.xl.mphelper.shard;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ParameterUtils;
import com.xl.mphelper.annonations.TableShard;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.xl.mphelper.shard.TableShardInterceptor.REFLECTOR_FACTORY;
import static org.apache.ibatis.reflection.SystemMetaObject.DEFAULT_OBJECT_FACTORY;
import static org.apache.ibatis.reflection.SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY;


/**
 * @author tanjl11
 * @date 2021/12/08 14:49
 */
@Component
@Slf4j
@Intercepts(
        {@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
        ),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
                )
        })
@ConditionalOnExpression("${mphelper.shard-support:false}")
public class TableShardQueryInterceptor implements Interceptor {

    /**
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!TableShardHolder.hasQueryTableShard()) {
            return invocation.proceed();
        }
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();
        final Executor executor = (Executor) target;
        Object parameter = args[1];
        MappedStatement ms = (MappedStatement) args[0];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        BoundSql boundSql;
        if (args.length == 4) {
            boundSql = ms.getBoundSql(parameter);
        } else {
            boundSql = (BoundSql) args[5];
        }
        MetaObject metaObject = MetaObject.forObject(boundSql, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, REFLECTOR_FACTORY);
        Class<? extends BaseMapper> mapperClass = TableShardInterceptor.getMapperClass(ms);
        if (!mapperClass.isAnnotationPresent(TableShard.class)) {
            return invocation.proceed();
        }
        TableShard annotation = mapperClass.getAnnotation(TableShard.class);
        Set<String> tableNames = TableShardInterceptor.getTableNames(boundSql, annotation);
        String sqlStr = (String) metaObject.getValue("sql");
        Set<String> suffix = TableShardHolder.getSuffix();
        List<Object> res = new ArrayList<>();
        Map<String, List<Object>> tempRecord = new HashMap<>();
        for (String s : suffix) {
            for (String tableName : tableNames) {
                metaObject.setValue("sql", sqlStr.replaceAll(tableName, tableName + "_" + s));
            }
            CacheKey cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
            List<Object> query = executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
            tempRecord.put(s, query);
        }
        Optional<IPage> page = ParameterUtils.findPage(parameter);
        if (page.isPresent()) {
            IPage iPage = page.get();
            if (iPage.getTotal() == 0) {
                long total = 0;
                for (Map.Entry<String, List<Object>> entry : tempRecord.entrySet()) {
                    List<Object> tempRes = entry.getValue();
                    long thisTableTotal = Long.parseLong(tempRes.get(0).toString());
                    total += thisTableTotal;
                }
                return Collections.singletonList(total);
            }
        }
        for (List<Object> value : tempRecord.values()) {
            res.addAll(value);
        }
        return res;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }
}
