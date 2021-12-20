package com.xl.tracer.plugin.sql;


import com.mysql.cj.jdbc.JdbcPreparedStatement;
import com.xl.tracer.plugin.IPlugin;
import com.xl.tracer.plugin.MatchPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * @author tanjl11
 * @date 2021/12/14 22:06
 */
public class MysqlPlugin implements IPlugin {
    @Override
    public String name() {
        return "mysql";
    }

    /**
     * createStatement
     * statement.setParameter
     * execute
     * @return
     */
    @Override
    public MatchPoint[] points() {
        return new MatchPoint[]{new MatchPoint() {
            @Override
            public ElementMatcher<TypeDescription> buildTypesMatcher() {
                return ElementMatchers.isSubTypeOf(JdbcPreparedStatement.class);
            }

            @Override
            public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                return ElementMatchers.isMethod().and(ElementMatchers.named("execute")
                )
                        ;
            }
        }};
    }

    @Override
    public Class adviceClass() {
        return MysqlAdvice.class;
    }
}
