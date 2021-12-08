package com.xl.mphelper.shard;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xl.mphelper.util.ApplicationContextHolder;

import java.util.*;

/**
 * @author tanjl11
 * @date 2021/10/18 15:18
 * 用于自定义表名，在与sql交互前使用
 * 否则默认走拦截器的获取参数逻辑
 */
public class TableShardHolder {
    protected static ThreadLocal<Map<String, Object>> HOLDER = ThreadLocal.withInitial(HashMap::new);
    protected static ThreadLocal<Set<String>> QUERY_HOLDER = ThreadLocal.withInitial(HashSet::new);
    private static String IGNORE_FLAG = "##ignore@@";
    private static String HASH_LENGTH = "##hash_length@@";

    //默认以_拼接
    public static void putVal(Class entityClazz, String suffix) {
        if (entityClazz.isAnnotationPresent(TableName.class)) {
            TableName tableName = (TableName) entityClazz.getAnnotation(TableName.class);
            String value = tableName.value();
            if (value.equals(IGNORE_FLAG) || value.equals(HASH_LENGTH)) {
                throw new IllegalStateException("conflict with exists flags,try another table name");
            }
            //hash策略处理
            String res = value + "_" + suffix;
            if (hashTableLength() != null) {
                ITableShardStrategy tableShardStrategy = TableShardInterceptor.SHARD_STRATEGY.computeIfAbsent(ITableShardStrategy.HashStrategy.class, e -> (ITableShardStrategy) ApplicationContextHolder.getBeanOrInstance(e));
                res = tableShardStrategy.routingTable(value, suffix);
            }
            HOLDER.get().put(value, res);
        }
    }

    public static void ignore() {
        HOLDER.get().put(IGNORE_FLAG, "");
    }

    protected static boolean isIgnore() {
        return HOLDER.get().containsKey(IGNORE_FLAG);
    }

    public static void resetIgnore() {
        HOLDER.get().remove(IGNORE_FLAG);
    }

    public static void remove(Class entityClazz) {
        if (entityClazz.isAnnotationPresent(TableName.class)) {
            TableName tableName = (TableName) entityClazz.getAnnotation(TableName.class);
            String value = tableName.value();
            HOLDER.get().remove(value);
        }
    }

    protected static String getReplaceName(String tableName) {
        return (String) HOLDER.get().get(tableName);
    }

    protected static boolean containTable(String tableName) {
        return HOLDER.get().containsKey(tableName);
    }

    protected static boolean hasVal() {
        return HOLDER.get() != null && !HOLDER.get().isEmpty();
    }

    public static void clearAll() {
        HOLDER.remove();
    }

    public static void hashTableLength(int length) {
        HOLDER.get().put(HASH_LENGTH, length);
    }

    protected static Integer hashTableLength() {
        return (Integer) HOLDER.get().get(HASH_LENGTH);
    }

    public static void clearHashTableLength() {
        HOLDER.get().remove(HASH_LENGTH);
    }

    public static void putQueryTableShard(String... suffix) {
        Set<String> strings = QUERY_HOLDER.get();
        for (String s : suffix) {
            strings.add(s);
        }
        QUERY_HOLDER.set(strings);
    }

    public static void putQueryTableShard(Collection<String> suffix) {
        Set<String> strings = QUERY_HOLDER.get();
        strings.addAll(suffix);
        QUERY_HOLDER.set(strings);
    }

    public static boolean hasQueryTableShard() {
        return !QUERY_HOLDER.get().isEmpty();
    }

    public static void clearQueryTableShard() {
        QUERY_HOLDER.remove();
    }

    public static void removeQueryTableShard(String key) {
        QUERY_HOLDER.get().remove(key);
    }

    protected static Set<String> getSuffix() {
        return QUERY_HOLDER.get();
    }
}
