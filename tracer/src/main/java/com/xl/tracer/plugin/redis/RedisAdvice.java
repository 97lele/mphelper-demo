package com.xl.tracer.plugin.redis;


import com.xl.tracer.log.TraceLog;
import com.xl.tracer.log.TraceLogCollector;
import com.xl.tracer.trace.TracingContext;
import net.bytebuddy.asm.Advice;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author tanjl11
 * @date 2021/12/15 15:12
 */

public class RedisAdvice {
    @Advice.OnMethodEnter()
    public static void enter(@Advice.Origin("#t") String className, @Advice.AllArguments Object[] args, @Advice.Origin("#m") String methodName) {
        TraceLog traceLog = TraceLogCollector.createLog(className, methodName, "redis");
        if (traceLog != null) {
            StringBuilder builder = new StringBuilder();
            for (Object o : args) {
                if (o instanceof byte[]) {
                    byte[] t = (byte[]) o;
                    builder.append(new String(t, Charset.defaultCharset())).append(",");
                } else {
                    builder.append(o.toString()).append(",");
                }
            }
            traceLog.setLogInfo(builder.deleteCharAt(builder.length() - 1).toString());
            TracingContext.push(traceLog, className, methodName);
        }

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
