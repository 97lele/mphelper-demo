package com.xl.tracer.plugin.rpc;

import com.xl.tracer.log.TraceLog;
import com.xl.tracer.trace.TracingContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Stack;

/**
 * spring环境需要扫描并加载到这个包
 *
 * @author tanjl11
 * @date 2021/12/15 14:03
 */
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Stack<TraceLog> stack = TracingContext.getStack();
        requestTemplate.header(TracingContext.TRACE_ID, TracingContext.getOrCreateTraceId());
        if (!stack.isEmpty()) {
            requestTemplate.header(TracingContext.PARENT_SPAN_ID, stack.peek().getSpanId());
            requestTemplate.header(TracingContext.RECORD_FLAG, TracingContext.isRecord() ? "N" : "Y");
            requestTemplate.header(TracingContext.LEVEL, TracingContext.getLevelStr());
        }
    }
}
