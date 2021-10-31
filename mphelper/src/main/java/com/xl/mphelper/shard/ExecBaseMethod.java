package com.xl.mphelper.shard;

import com.xl.mphelper.annonations.TableShardIgnore;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author tanjl11
 * @date 2021/10/26 18:43
 * 当找不到方法时候，可能是分页类型的，需要额外处理
 */
public class ExecBaseMethod {

    protected MethodInfo genMethodInfo(Method[] candidateMethods, String curMethodName) {
        Method curMethod = null;
        for (Method method : candidateMethods) {
            if (method.getName().equals(curMethodName)) {
                curMethod = method;
                break;
            }
        }
        if (curMethod == null) {
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.shouldIgnore = true;
            return methodInfo;
        }
        boolean shouldIgnore = curMethod.isAnnotationPresent(TableShardIgnore.class);
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.shouldIgnore = shouldIgnore;
        methodInfo.parameters = curMethod.getParameters();
        return methodInfo;
    }

    public static class MethodInfo {
        protected boolean shouldIgnore;
        protected Parameter[] parameters;
    }
}
