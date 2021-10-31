package com.xl.mphelper.shard;

import com.xl.mphelper.annonations.TableShardIgnore;

import java.lang.reflect.Method;

/**
 * 适配 pageInfo 分页
 * mybatis-plus的分页插件没测试过是否适配
 * 规定了每个mapper只能用一种分页插件
 *
 * @author tanjl11
 * @date 2021/10/26 19:06
 */
public class ExecPageInfoMethod extends ExecBaseMethod {
    private static String SUFFIX = "_COUNT";

    @Override
    protected MethodInfo genMethodInfo(Method[] candidateMethods, String curMethodName) {
        MethodInfo m = super.genMethodInfo(candidateMethods, curMethodName);
        Method method = null;
        if (m == null || m.parameters == null) {
            for (Method candidateMethod : candidateMethods) {
                if ((candidateMethod.getName() + SUFFIX).equals(curMethodName)) {
                    method = candidateMethod;
                }
            }
        }
        if (method == null) {
            return m;
        }
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.parameters = method.getParameters();
        methodInfo.shouldIgnore = method.isAnnotationPresent(TableShardIgnore.class);
        return methodInfo;
    }
}
