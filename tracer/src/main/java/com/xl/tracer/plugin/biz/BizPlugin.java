package com.xl.tracer.plugin.biz;

import com.xl.tracer.Entrance;
import com.xl.tracer.plugin.IPlugin;
import com.xl.tracer.plugin.MatchPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author tanjl11
 * @date 2021/12/14 16:25
 * 只代理业务模块
 */
public class BizPlugin implements IPlugin {
    @Override
    public String name() {
        return "biz";
    }

    public MatchPoint buildBasePoint(String path) {
        return new MatchPoint() {
            @Override
            public ElementMatcher<TypeDescription> buildTypesMatcher() {
                return ElementMatchers.nameStartsWith(path).
                        and(ElementMatchers.not(ElementMatchers.nameStartsWith("com.midea.cloud.agent")))
                        .and(ElementMatchers.isAnnotatedWith(ElementMatchers.named("org.springframework.web.bind.annotation.RestController"))
                                .or(ElementMatchers.isAnnotatedWith(ElementMatchers.named("org.springframework.stereotype.Service")))
                                .or(ElementMatchers.isAnnotatedWith(ElementMatchers.named("org.springframework.stereotype.Controller")))
                        )
                        ;
            }

            @Override
            public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                return ElementMatchers.isMethod()
                        .and(ElementMatchers.not(ElementMatchers.named("destroy")))
                        .and(ElementMatchers.not(ElementMatchers.named("afterPropertiesSet")))
                        .and(ElementMatchers.any());
            }
        };
    }

    @Override
    public MatchPoint[] points() {
        String bizPath = Entrance.BIZ_PATH;
        String[] split = bizPath.split(",");
        MatchPoint[] matchPoints = new MatchPoint[split.length];
        for (int i = 0; i < split.length; i++) {
            matchPoints[i] = buildBasePoint(split[0]);
        }
        return matchPoints;
    }

    @Override
    public Class adviceClass() {
        return BizAdvice.class;
    }
}
