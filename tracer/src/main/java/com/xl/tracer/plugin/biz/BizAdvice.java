package com.xl.tracer.plugin.biz;


import com.xl.tracer.log.TraceLog;
import com.xl.tracer.log.TraceLogCollector;
import com.xl.tracer.trace.TracingContext;
import net.bytebuddy.asm.Advice;
import org.slf4j.MDC;
import java.util.Arrays;
import java.util.Stack;


/**
 * @author tanjl11
 * @date 2021/12/14 16:27
 */
public class BizAdvice {
    /**
     * 收集概率
     */

    @Advice.OnMethodEnter()
    public static void enter(@Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName) {
        Stack<TraceLog> stack = TracingContext.getStack();
        boolean record = TracingContext.isRecord();
        TracingContext.calculateLevel();
        if (record) {
            TraceLog log = new TraceLog();
            log.setClassName(className);
            log.setMethodName(methodName);
            log.setType("biz");
            if (stack.isEmpty()) {
                //如果已经有traceId，可能是多线程开启的,也有可能是远程服务,此时要从MDC里面拿东西
                String parentSpanId = TracingContext.getParentSpanId();
                String traceId = TracingContext.getOrCreateTraceId();
                log.setParentSpanId(parentSpanId);
                log.setTraceId(traceId);
                if (TracingContext.hasPreEnv()) {
                    //生成自己的spanId
                    log.setSpanId(TracingContext.genSpanId());
                } else {
                    //代表是第一次
                    log.setSpanId(traceId);
                }
                //如果已经有了，就直接拿上一个;
            } else {
                TraceLog parent = stack.peek();
                log.setTraceId(parent.getTraceId());
                log.setParentSpanId(parent.getSpanId());
                log.setSpanId(TracingContext.genSpanId());
            }
            //把父id设置，给下面线程用
            MDC.put(TracingContext.PARENT_SPAN_ID, log.getSpanId());
            log.setStartTime(System.currentTimeMillis());
            TracingContext.push(log, className, methodName);
            //计算当前栈
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName,@Advice.Thrown Throwable thrown) {
        TraceLog pop = TraceLogCollector.pop(className,methodName);
        if (thrown != null) {
            pop.setErrMsg(Arrays.toString(thrown.getStackTrace()));
        }
        if (pop.getLevel() == 0 && !TracingContext.isRecord()) {
            TracingContext.clear();
            return;
        }
        TraceLogCollector.collect(pop);
    }

}
