package com.xl.tracer.plugin.rpc;

import com.xl.tracer.plugin.IPlugin;
import com.xl.tracer.plugin.MatchPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tanjl11
 * @date 2021/12/17 21:58
 */
public class SpringMvcPlugin implements IPlugin {
    @Override
    public String name() {
        return "springmvc";
    }

    @Override
    public MatchPoint[] points() {
        return new MatchPoint[]{
                new MatchPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        return ElementMatchers.named("org.springframework.web.servlet.DispatcherServlet");
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.named("doService")
                                .and(ElementMatchers.takesArgument(0, HttpServletRequest.class))
                                ;
                    }
                }
        };
    }

    @Override
    public Class adviceClass() {
        return SpringMvcAdvice.class;
    }
}
