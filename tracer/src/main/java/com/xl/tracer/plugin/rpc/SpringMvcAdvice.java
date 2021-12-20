package com.xl.tracer.plugin.rpc;

import com.xl.tracer.trace.TracingContext;
import com.xl.tracer.util.StringUtils;
import net.bytebuddy.asm.Advice;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ThreadLocalRandom;

import static com.xl.tracer.Entrance.RECORD_RATE;

/**
 * @author tanjl11
 * @date 2021/12/17 21:53
 */
public class SpringMvcAdvice {
    @Advice.OnMethodEnter()
    public static void enter(@Advice.Origin("#t") String className, @Advice.Argument(0) HttpServletRequest request, @Advice.Origin("#m") String methodName) {
        TracingContext.logger.trace("springmvc,{},{}",className,methodName);
        String header = request.getHeader(TracingContext.RECORD_FLAG);
        if ("N".equals(header)) {
            TracingContext.notRecord();
            return;
        }
        String traceId = request.getHeader(TracingContext.TRACE_ID);
        if (!StringUtils.isEmpty(traceId)) {
            String parentSpanId = request.getHeader(TracingContext.PARENT_SPAN_ID);
            String level = request.getHeader(TracingContext.LEVEL);
            TracingContext.setRemoteInfo(traceId, parentSpanId, level);
        } else {
            if (TracingContext.isRecord()) {
                //判断是否需要记录
                double rate = ThreadLocalRandom.current().nextInt(100);
                TracingContext.logger.trace("链路随机值,{}", rate);
                if (rate > RECORD_RATE) {
                    TracingContext.notRecord();
                    return;
                }
            }
        }
    }
}
