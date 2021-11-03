package com.xl.mphelper.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tanjl11
 * @date 2021/10/27 11:34
 */
@Component
public class ApplicationContextHolder implements ApplicationContextAware {
    public static ApplicationContext context;
    private static final ReflectionFactory delegat = ReflectionFactory.getReflectionFactory();
    private static final Map<Class, Object> OBJECT_CLASS_CACHE = new ConcurrentHashMap<>();


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBeanOrInstance(Class<T> invokeClass) {
        T bean = getBean(invokeClass);
        if (bean == null) {
            bean = (T) OBJECT_CLASS_CACHE.computeIfAbsent(invokeClass, k -> {
                try {
                    Constructor<?> classConstructor = invokeClass.getConstructor();
                    //因为调用的是方法，无需调用构造函数、初始化代码
                    Constructor<?> constructor = delegat.newConstructorForSerialization(k, classConstructor);
                    ReflectionUtils.makeAccessible(constructor);
                    return (T) constructor.newInstance();
                } catch (Exception m) {
                    throw new IllegalStateException(invokeClass + "没有默认的空构造器");
                }
            });
        }
        return bean;
    }

    public static <T> T getBean(Class<T> invokeClass) {
        try {
            return context.getBean(invokeClass);
        } catch (BeansException e) {
            return null;
        }
    }
}
