package com.xl.mphelper.service;

import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.*;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xl.mphelper.mapper.CustomMapper;
import com.xl.mphelper.shard.Shardable;
import com.xl.mphelper.shard.TableShardHolder;
import com.xl.mphelper.util.ApplicationContextHolder;
import com.xl.mphelper.util.SnowflakeIds;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author tanjl11
 * @date 2021/06/23 18:46
 * 在原有基础上添加了更新空值的方法，忽略了mybatis-plus的ignore属性
 * 用于特殊场景
 * 1.指定列更新空值
 * 2.所有列更新空值
 * 一般场景用updateBatchById、updateById即可
 * 添加保存方法，把更新、删除、新增的方法进行整合
 */
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Slf4j
public class CustomServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    private final WrapperHelper helper = new WrapperHelper();


    @Transactional(rollbackFor = Exception.class)
    public int saveBatchPlus(Collection<T> entity, Wrapper<T> queryWrapper) {
        return saveBatchPlus(entity, () -> list(queryWrapper), null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public int saveBatchPlus(Collection<T> entity, Wrapper<T> queryWrapper, Function<T, ?> uniqueKey) {
        return saveBatchPlus(entity, () -> list(queryWrapper), uniqueKey, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public int saveBatchPlus(Collection<T> entity, Wrapper<T> queryWrapper, Function<T, ?> uniqueKey, Supplier<Serializable> idGenerator) {
        return saveBatchPlus(entity, () -> list(queryWrapper), uniqueKey, idGenerator);
    }

    /**
     * @param values        插入的项
     * @param getExistsData 对比的数据范围
     * @param uniqueKey     updateList赋值的操作，根据该项形成map，然后把对应的数据库ID赋值到符合的value
     * @param idGenerator   ID生成的操作，没有默认是雪花
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int saveBatchPlus(Collection<T> values, Supplier<Collection<T>> getExistsData, Function<T, ?> uniqueKey, Supplier<Serializable> idGenerator) {
        String keyProperty = getKeyPropertyFromLists(values);
        boolean deleteAll = false;
        Collection<T> list = getExistsData.get();
        if (CollectionUtils.isEmpty(values)) {
            keyProperty = getKeyPropertyFromLists(list);
            deleteAll = true;
        }
        if (keyProperty == null) {
            throw new IllegalStateException("not keyProperty find in param");
        }
        //以传输过来的数据为准，不在传输范围内的删掉
        Set<Serializable> existsIds = new HashSet<>();
        for (T t : list) {
            Serializable idVal = (Serializable) ReflectionKit.getFieldValue(t, keyProperty);
            existsIds.add(idVal);
        }
        if (deleteAll) {
            removeByIds(existsIds);
            return existsIds.size();
        }
        if (idGenerator == null) {
            idGenerator = () -> SnowflakeIds.generate();
        }
        //赋值updateList
        if (uniqueKey != null) {
            Map<?, T> map = values.stream().collect(Collectors.toMap(uniqueKey, Function.identity(), (x1, x2) -> x1));
            for (T t : values) {
                T dataValue = map.get(uniqueKey.apply(t));
                Serializable idVal = (Serializable) ReflectionKit.getFieldValue(dataValue, keyProperty);
                setId(() -> idVal, keyProperty, t);
            }
        }
        //留下数据库存在的
        List<T> updateList = new ArrayList<>(values.size());
        List<T> addList = new ArrayList<>(values.size());
        for (T t : values) {
            Serializable idVal = (Serializable) ReflectionKit.getFieldValue(t, keyProperty);
            if (idVal != null) {
                updateList.add(t);
                existsIds.remove(idVal);
            } else {
                setId(idGenerator, keyProperty, t);
                addList.add(t);
            }
        }
        if (CollectionUtils.isNotEmpty(existsIds)) {
            removeByIds(existsIds);
        }
        M baseMapper = getBaseMapper();
        if (CollectionUtils.isNotEmpty(updateList)) {
            if (baseMapper instanceof CustomMapper) {
                ((CustomMapper<T>) baseMapper).updateBatchByIds(updateList);
            } else {
                updateBatchById(updateList);
            }

        }
        if (CollectionUtils.isNotEmpty(addList)) {
            if (baseMapper instanceof CustomMapper) {
                ((CustomMapper<T>) baseMapper).insertBatch(addList);
            } else {

                saveBatch(addList);
            }
        }
        return addList.size() + existsIds.size() + updateList.size();
    }

    private void setId(Supplier<Serializable> idGenerator, String keyProperty, T t) {
        Field field = ReflectionUtils.findField(t.getClass(), keyProperty);
        if (Objects.nonNull(field)) {
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, t, idGenerator.get());
        }
    }

    private String getKeyPropertyFromLists(Collection<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Class<?> cls = list.iterator().next().getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
        return tableInfo.getKeyProperty();
    }

    /**
     * 分表新增
     * @param entityList
     * @return
     */
    public boolean saveBatchShard(Collection<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        T param = entityList.iterator().next();
        if (param instanceof Shardable) {
            Collection<Shardable> shardables = (Collection<Shardable>) entityList;
            shardables.stream().collect(Collectors.groupingBy(Shardable::suffix)).forEach((k, v) -> {
                super.saveBatch((Collection<T>) v);
            });
            return true;
        }
        return false;
    }

   public boolean updateBatchByShard(Collection<T> entityList){
       if (CollectionUtils.isEmpty(entityList)) {
           return false;
       }
       T param = entityList.iterator().next();
       if (param instanceof Shardable) {
           Collection<Shardable> shardables = (Collection<Shardable>) entityList;
           shardables.stream().collect(Collectors.groupingBy(Shardable::suffix)).forEach((k, v) -> {
               super.updateBatchById((Collection<T>) v);
           });
           return true;
       }
       return false;
   }

    /**
     * 分表删除
     * @param entityList
     * @return
     */
    public boolean removeByShard(Collection<T> entityList){
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        T param = entityList.iterator().next();
        if (param instanceof Shardable) {
            Collection<Shardable> shardables = (Collection<Shardable>) entityList;
            String keyProperty = getKeyPropertyFromLists(entityList);
            shardables.stream().collect(Collectors.groupingBy(Shardable::suffix)).forEach((k, v) -> {
                TableShardHolder.putVal(param.getClass(),k);
                List<Serializable> id=new ArrayList<>(v.size());;
                for (Shardable shardable : v) {
                    Serializable idValue = (Serializable) ReflectionKit.getFieldValue(shardable, keyProperty);
                    if(Objects.nonNull(idValue)){
                        id.add(idValue);
                    }
                }
                super.removeByIds(id);
                TableShardHolder.remove(param.getClass());
            });
            return true;
        }
        return false;
    }

    /**
     * 指定字段更新空值
     *
     * @param entity
     * @param functions
     * @return
     */
    public boolean updateIfNullProperties(T entity, SFunction<T, ?>... functions) {
        String[] columns = Arrays.stream(functions).map(helper::columnToString).toArray(String[]::new);
        return updateIfNullProperties(entity, false, columns);
    }

    /**
     * 批量更新
     *
     * @param entity
     * @param functions
     * @return
     */
    public boolean updateIfNullPropertiesBatch(Collection<T> entity, SFunction<T, ?>... functions) {
        String[] columns = Arrays.stream(functions).map(helper::columnToString).toArray(String[]::new);
        return updateIfNullPropertiesBatch(entity, false, columns);
    }

    /**
     * 不管 FieldStrategy，只要是空就更新
     *
     * @param entity
     * @return
     */
    public boolean updateIfNullProperties(T entity) {
        return updateIfNullProperties(entity, true);
    }

    /**
     * 批量更新，不管FieldStrategy
     *
     * @param entity
     * @return
     */
    public boolean updateIfNullPropertiesBatch(Collection<T> entity) {
        return updateIfNullPropertiesBatch(entity, true);
    }

    /**
     * 特定的列是空的就会更新，其他其他字段
     *
     * @param entity
     * @param columns
     * @return
     */
    public boolean updateIfNullProperties(T entity, String... columns) {
        return updateIfNullProperties(entity, false, columns);
    }

    public boolean updateIfNullPropertiesBatch(Collection<T> entity, String... columns) {
        return updateIfNullPropertiesBatch(entity, false, columns);
    }

    private boolean updateIfNullPropertiesBatch(Collection<T> entityList, boolean allField, String... columns) {
        String sqlStatement = getSqlStatement(SqlMethod.UPDATE);
        return executeBatch(entityList, DEFAULT_BATCH_SIZE, (sqlSession, entity) -> {
            Map<String, Object> map = new HashMap<>(1);
            UpdateWrapper<T> updateWrapper = genUpdateWrapper(entity, allField, columns);
            map.put(Constants.WRAPPER, updateWrapper);
            sqlSession.update(sqlStatement, map);
        });
    }

    private boolean updateIfNullProperties(T entity, boolean allField, String... columns) {
        UpdateWrapper<T> updateWrapper = genUpdateWrapper(entity, allField, columns);
        if (updateWrapper == null) {
            return false;
        }
        return update(updateWrapper);
    }


    private UpdateWrapper<T> genUpdateWrapper(T entity, boolean allField, String[] columns) {
        Class<?> cls = entity.getClass();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
        String keyProperty = tableInfo.getKeyProperty();
        Assert.notEmpty(keyProperty, "execute fail: can't find column for id from entity!");
        Set<String> columnSet = Arrays.stream(columns).collect(Collectors.toSet());
        Object idVal = ReflectionKit.getFieldValue(cls, tableInfo.getKeyProperty());
        if (Objects.isNull(idVal)) {
            return null;
        }
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(tableInfo.getKeyColumn(), idVal);
        for (TableFieldInfo tableFieldInfo : tableInfo.getFieldList()) {
            String columnName = tableFieldInfo.getColumn();
            Object columnValue = ReflectionKit.getFieldValue(entity, tableFieldInfo.getProperty());
            //如果为空值且更新为空列包含或允许所有列更新为空，添加
            boolean canSetNull = columnSet.contains(columnName) || allField;
            if (Objects.isNull(columnValue) && canSetNull) {
                updateWrapper.set(columnName, null);
            }
            //不为空的值，添加
            if (Objects.nonNull(columnValue)) {
                updateWrapper.set(columnName, columnValue);
            }
        }
        return updateWrapper;
    }


    public static void doInTransaction(Runnable runnable) {
        doInTransaction(runnable, null, null);
    }

    public static void doInTransaction(Runnable runnable, Runnable onSuccess, Consumer<Exception> onFail) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        PlatformTransactionManager transactionManager = ApplicationContextHolder.getBean(PlatformTransactionManager.class);
        TransactionStatus transaction = transactionManager.getTransaction(transactionDefinition);
        boolean success = true;
        try {
            runnable.run();
            transactionManager.commit(transaction);
        } catch (Exception e) {
            success = false;
            transactionManager.rollback(transaction);
            log.error("执行出错", e);
            if (Objects.nonNull(onFail)) {
                onFail.accept(e);
            }
        }
        if (Objects.nonNull(onSuccess) && success) {
            onSuccess.run();
        }
    }

    /**
     * 这个继承只是为了获取由lambda转为列名的方法
     */
    @SuppressWarnings("rawtypes")
    static class WrapperHelper extends AbstractLambdaWrapper {
        @Override
        protected AbstractWrapper instance() {
            return null;
        }

        @Override
        public String columnToString(SFunction column) {
            return super.columnToString(column);
        }
    }
}
