package com.xl.mphelper.dynamic;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @author tanjl11
 * @date 2021/10/27 11:07
 */
public class DynamicDataSourceHolder {
    private static ThreadLocal tl = new TransmittableThreadLocal<>();

    public static void switchDataSource(String key) {
        tl.set(key);
    }

    protected static String getCurrentKey() {
        return (String) tl.get();
    }

    public static void clear() {
        tl.remove();
    }
}
