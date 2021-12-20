package com.xl.tracer.plugin;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * @author tanjl11
 * @date 2021/12/14 16:23
 */
public interface MatchPoint {
    /**
     * 类匹配规则
     * @return
     */
    ElementMatcher<TypeDescription> buildTypesMatcher();

    /**
     * 方法匹配规则
     * @return
     */
    ElementMatcher<MethodDescription> buildMethodsMatcher();
}
