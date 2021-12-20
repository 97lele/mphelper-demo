package com.xl.tracer.plugin.sql;


import com.xl.tracer.log.TraceLog;
import com.xl.tracer.log.TraceLogCollector;
import com.xl.tracer.trace.TracingContext;
import net.bytebuddy.asm.Advice;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Arrays;

/**
 * @author tanjl11
 * @date 2021/12/14 16:27
 */
public class MybatisAdvice {

    @Advice.OnMethodEnter()
    public static void enter(@Advice.Argument(0) MappedStatement statement
    ) {
        String id = statement.getId();
        int endIndex = id.lastIndexOf(".");
        String mapperMethod = id.substring(endIndex + 1, id.length());
        String mapperName = id.substring(0, endIndex);
        TraceLog log = TraceLogCollector.createLog(mapperName, mapperMethod, "mybatis");
        TracingContext.push(log,mapperName,mapperMethod);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Thrown Throwable thrown,@Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName) {
        TraceLog pop = TraceLogCollector.pop(className,methodName);
        if (pop != null) {
            if (thrown != null) {
                pop.setErrMsg(Arrays.toString(thrown.getStackTrace()));
            }
            TraceLogCollector.collect(pop);
        }
    }

}
