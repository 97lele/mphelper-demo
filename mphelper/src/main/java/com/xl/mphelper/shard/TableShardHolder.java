package com.xl.mphelper.shard;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tanjl11
 * @date 2021/10/18 15:18
 * 用于自定义表名，在与sql交互前使用
 * 否则默认走拦截器的获取参数逻辑
 */
public class TableShardHolder {
    protected static ThreadLocal<Map<String, String>> HOLDER = ThreadLocal.withInitial(HashMap::new);
    private static String INGORE_FLAG = "##ingore@@";

    //默认以_拼接
    public static void putVal(Class entityClazz, String suffix) {
        if (entityClazz.isAnnotationPresent(TableName.class)) {
            TableName tableName = (TableName) entityClazz.getAnnotation(TableName.class);
            String value = tableName.value();
            if(value.equals(INGORE_FLAG)){
                throw new IllegalStateException("conflict with ignore flag,try another table name");
            }
            HOLDER.get().put(value, value + "_" + suffix);
        }
    }

    public static void ignore() {
        HOLDER.get().put(INGORE_FLAG, "");
    }

    protected static boolean isIgnore() {
        return HOLDER.get().containsKey(INGORE_FLAG);
    }

    public static void resetIgnore() {
        HOLDER.get().remove(INGORE_FLAG);
    }

    public static void remove(Class entityClazz) {
        if (entityClazz.isAnnotationPresent(TableName.class)) {
            TableName tableName = (TableName) entityClazz.getAnnotation(TableName.class);
            String value = tableName.value();
            HOLDER.get().remove(value);
        }
    }

    protected static String getReplaceName(String tableName) {
        return HOLDER.get().get(tableName);
    }

    protected static boolean containTable(String tableName) {
        return HOLDER.get().containsKey(tableName);
    }

    protected static boolean hasVal() {
        return HOLDER.get() != null && !HOLDER.get().isEmpty();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
