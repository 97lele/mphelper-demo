package com.xl.tracer.trace;


import com.xl.tracer.log.TraceLog;
import com.xl.tracer.util.StringUtils;
import net.bytebuddy.utility.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Stack;
import java.util.UUID;

/**
 * @author tanjl11
 * @date 2021/12/14 16:32
 */
public class TracingContext {
    public final static Logger logger = LoggerFactory.getLogger(TracingContext.class);
    public static String TRACE_ID = "tracing-id";
    public static String PARENT_SPAN_ID = "parent-span-id";
    //层级
    public static String LEVEL = "tracing-level";
    public static String RECORD_FLAG = "record-flag";

    private static ThreadLocal<Stack<TraceLog>> LOCAL_STACK = new ThreadLocal<Stack<TraceLog>>();

    private static String genUniqueId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String genSpanId() {
        return RandomString.make();
    }

    public static TraceLog pop() {
        Stack<TraceLog> stack = getStack();
        if (!stack.isEmpty()) {
            return stack.pop();
        }
        return null;
    }

    public static void clear() {
        MDC.remove(TracingContext.LEVEL);
        MDC.remove(TracingContext.PARENT_SPAN_ID);
        MDC.remove(TracingContext.TRACE_ID);
        MDC.remove(TracingContext.RECORD_FLAG);
        TracingContext.getStack().clear();
    }

    public static void notRecord() {
        logger.trace("链路取消记录");
        MDC.put(TracingContext.RECORD_FLAG, "N");
    }

    public static boolean isRecord() {
        return !"N".equals(MDC.get(TracingContext.RECORD_FLAG));
    }

    public static void push(TraceLog log, String className, String methodName) {
        logger.trace("尝试入栈:{},{},{}",log,className,methodName);
        if (log != null) {
            Stack<TraceLog> stack = getStack();
            stack.push(log);
        }

    }

    public static boolean isEmpty() {
        return LOCAL_STACK.get().isEmpty();
    }

    public static int subLevel() {
        String s = MDC.get(LEVEL);
        int level = Integer.parseInt(s);
        MDC.put(LEVEL, String.valueOf(level - 1));
        return level;
    }

    public static void calculateLevel() {
        String s = MDC.get(LEVEL);
        if (s == null) {
            MDC.put(LEVEL, "1");
        } else {
            MDC.put(LEVEL, String.valueOf(Integer.parseInt(s) + 1));
        }
    }

    public static String getLevelStr() {
        return MDC.get(LEVEL);
    }

    public static Stack<TraceLog> getStack() {
        Stack<TraceLog> stack = LOCAL_STACK.get();
        if (stack == null) {
            stack = new Stack<>();
            LOCAL_STACK.set(stack);
        }
        return stack;
    }

    public static void setRemoteInfo(String traceId, String parentSpanId, String level) {
        MDC.put(PARENT_SPAN_ID, parentSpanId);
        MDC.put(TRACE_ID, traceId);
        MDC.put(LEVEL, level);
    }

    public static void setRemoteTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static void setRemoteParentSpan(String parentSpanId) {
        MDC.put(PARENT_SPAN_ID, parentSpanId);
    }

    public static String getParentSpanId() {
        String parentId = MDC.get(PARENT_SPAN_ID);
        return StringUtils.isEmpty(parentId) ? getOrCreateTraceId() : parentId;
    }

    public static boolean hasPreEnv() {
        return hasParentId() && hasTraceId();
    }

    public static boolean hasTraceId() {
        return MDC.get(TRACE_ID) != null;
    }

    public static boolean hasParentId() {
        return MDC.get(PARENT_SPAN_ID) != null;
    }

    public static String getOrCreateTraceId() {
        String traceId = MDC.get(TRACE_ID);
        if (StringUtils.isEmpty(traceId)) {
            traceId = genUniqueId();
            MDC.put(TRACE_ID, traceId);
        }
        return traceId;
    }
}
