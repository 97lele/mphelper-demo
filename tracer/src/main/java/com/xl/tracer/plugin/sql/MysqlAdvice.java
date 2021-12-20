package com.xl.tracer.plugin.sql;

import com.xl.tracer.log.TraceLog;
import com.xl.tracer.log.TraceLogCollector;
import com.xl.tracer.trace.TracingContext;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import com.mysql.cj.jdbc.JdbcPreparedStatement;
import com.mysql.cj.jdbc.ParameterBindings;
import net.bytebuddy.asm.Advice;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tanjl11
 * @date 2021/12/14 22:05
 */
public class MysqlAdvice {
    public final static Pattern PARSER = Pattern.compile("\\?");

    /**
     * 首先是获取当前的statement，然后退出，再设置每个预编译参数
     *
     * @param className
     * @param methodName
     */
    @Advice.OnMethodEnter()
    public static void enter(@Advice.This JdbcPreparedStatement thiz, @Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName) {
        if (thiz instanceof ClientPreparedStatement) {
            TraceLog log = TraceLogCollector.createLog(className, methodName, "mysql");
            if (log != null) {
                ClientPreparedStatement yo = (ClientPreparedStatement) thiz;
                try {
                    int count = thiz.getParameterMetaData().getParameterCount();
                    String preSql = yo.getPreparedSql();
                    if(preSql.length()>10000){
                        preSql=preSql.substring(0,10000);
                    }else{
                        preSql=preSql.replaceAll("\n","");
                        if (count >= 1) {
                            ParameterBindings parameterBindings = yo.getParameterBindings();
                            String[] split = new String[count];
                            for (int i = 1; i <= count; i++) {
                                String params = parameterBindings.getString(i);
                                split[i - 1] = "'" + params + "'";
                            }
                            Matcher matcher = PARSER.matcher(preSql);
                            StringBuffer buffer = new StringBuffer();
                            int i = 0;
                            while (matcher.find()) {
                                matcher.appendReplacement(buffer, split[i++]);
                            }
                            preSql = matcher.appendTail(buffer).toString();
                        }
                    }
                    log.setLogInfo(preSql);
                } catch (SQLException throwables) {
                }
                TracingContext.push(log,className,methodName);
            }
        }
    }


    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.This PreparedStatement thiz,@Advice.Thrown Throwable thrown,@Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName) {
        if (thiz instanceof ClientPreparedStatement) {
            TraceLog pop = TraceLogCollector.pop(className,methodName);
            if (pop != null) {
                if (thrown != null) {
                    pop.setErrMsg(Arrays.toString(thrown.getStackTrace()));
                }
                TraceLogCollector.collect(pop);
            }
        }
    }
}
