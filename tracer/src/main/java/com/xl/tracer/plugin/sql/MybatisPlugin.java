package com.xl.tracer.plugin.sql;
;
import com.xl.tracer.plugin.IPlugin;
import com.xl.tracer.plugin.MatchPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * @author tanjl11
 * @date 2021/12/16 14:22
 */
public class MybatisPlugin implements IPlugin {
    @Override
    public String name() {
        return "mybatis";
    }

    @Override
    public MatchPoint[] points() {
        return new MatchPoint[]{
                new MatchPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        return ElementMatchers.isSubTypeOf(Executor.class)
                                .and(ElementMatchers.nameStartsWith("org.apache.ibatis.executor"));
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.takesArgument(0, MappedStatement.class)
                                .and(ElementMatchers.named("query").and(ElementMatchers.takesArguments(6))
                                        .or(ElementMatchers.named("update").and(ElementMatchers.takesArguments(2)))
                                );
                    }
                }
        };
    }

    @Override
    public Class adviceClass() {
        return MybatisAdvice.class;
    }
}
