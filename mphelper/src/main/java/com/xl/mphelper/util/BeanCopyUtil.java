package com.xl.mphelper.util;

import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.objenesis.ObjenesisStd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tanjl11
 * @date 2021/11/23 14:14
 */
public class BeanCopyUtil {
    private static final ThreadLocal<ObjenesisStd> objStd = ThreadLocal.withInitial(ObjenesisStd::new);
    private static final ConcurrentHashMap<String, BeanCopier> CACHE = new ConcurrentHashMap<>();

    public static <T> T copy(Object source, Class target) {
        Object obj = newInstance(target);
        return copy(source,obj);
    }

    public static <T> T copy(Object source, Object target) {
        Class<?> sourceClass = source.getClass();
        String sourceName = sourceClass.getName();
        Class<?> targetClass = target.getClass();
        String targetName = targetClass.getName();
        BeanCopier copier = CACHE.computeIfAbsent(sourceName + targetName, (k) -> BeanCopier.create(sourceClass, targetClass, false));
        copier.copy(source, target, null);
        return (T) target;
    }

    public static <T> List<T> copyList(Collection<Object> source, Class target) {
        List<T> res = new ArrayList<>(source.size());
        for (Object o : source) {
            res.add(copy(o, target));
        }
        return res;
    }

    public static <T> T mapToBean(Map<String, Object> map, Class beanClass) {
        T o = newInstance(beanClass);
        BeanMap beanMap = BeanMap.create(o);
        beanMap.putAll(map);
        return o;
    }

    public static Map<String, Object> beanToMap(Object bean) {
        return BeanMap.create(bean);
    }

    private static <T> T newInstance(Class beanClass) {
        ObjenesisStd std = objStd.get();
        return (T) std.newInstance(beanClass);
    }
}
