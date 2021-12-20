package com.xl.tracer.log;

import com.xl.tracer.trace.TracingContext;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author tanjl11
 * @date 2021/12/15 16:57
 */
public class TraceLogCollector {

    private static final ReentrantLock lock = new ReentrantLock();
    private static final ArrayList<TraceLog> DATA_LIST = new ArrayList<>(500);
    private static Long LAST_SEND_TIME = System.currentTimeMillis();
    private static ThreadPoolExecutor ioThreadPool;

    static {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        //根据业务量调整线程数
        ioThreadPool = new ThreadPoolExecutor(cpuCount, cpuCount,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "trace-log-sender");
                        thread.setDaemon(true);
                        return thread;
                    }
                }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 用于非biz模块的日志建立
     *
     * @param className
     * @param methodName
     * @param type
     * @return
     */
    public static TraceLog createLog(String className, String methodName, String type) {
        boolean record = TracingContext.isRecord();
        TracingContext.calculateLevel();
        if (!record) {
            return null;
        }
        Stack<TraceLog> stack = TracingContext.getStack();
        if (stack.isEmpty() && !TracingContext.hasPreEnv()) {
            return null;
        }
        TraceLog log = new TraceLog();
        log.setClassName(className);
        log.setMethodName(methodName);
        log.setType(type);
        log.setSpanId(TracingContext.genSpanId());
        log.setStartTime(System.currentTimeMillis());
        String parentSpanId = null;
        String traceId = null;
        if (!stack.isEmpty()) {
            TraceLog parent = stack.peek();
            parentSpanId = parent.getSpanId();
            traceId = parent.getTraceId();
        } else {
            parentSpanId = TracingContext.getParentSpanId();
            traceId = TracingContext.getOrCreateTraceId();
        }
        log.setParentSpanId(parentSpanId);
        log.setTraceId(traceId);
        return log;
    }

    public static void collect(TraceLog pop) {
        if (pop != null) {
            DATA_LIST.add(pop);
            TracingContext.logger.trace("{},添加到缓存中,当前数量{},是否root:{}", pop, DATA_LIST.size(), pop.isRoot());
            //代表这次链路已经结束了
            if (pop.isRoot()) {
                TracingContext.clear();
                //开始异步构建并发送
                Runnable runnable = () -> {
                    lock.lock();
                    try {
                        long now = System.currentTimeMillis();
                        boolean dataFit = DATA_LIST.size() >= 100;
                        long l = now - LAST_SEND_TIME;
                        boolean timeFit = l >= 2000;
                        TracingContext.logger.trace("是否发送条件:{},{},相差毫秒数{}", dataFit, timeFit, l);
                        if (dataFit || timeFit) {
                            TracingContext.logger.trace("开始发送");
                            LogSenderFactory.getEsSender().batchSend(DATA_LIST);
                            TracingContext.logger.trace("发送结束");
                        }
                        LAST_SEND_TIME = now;
                        DATA_LIST.clear();
                    } catch (Exception e) {
                        TracingContext.logger.error("批量传送日志出错", e);
                    } finally {
                        lock.unlock();
                    }
                };
                CompletableFuture.runAsync(runnable, ioThreadPool);
            }
        }
    }

    public static TraceLog pop(String className, String methodName) {
        int level = TracingContext.subLevel();
        TracingContext.logger.trace("尝试弹出，当前层级,{},className,{},methodName,{}", level, className, methodName);
        TraceLog pop = null;
        if (TracingContext.isRecord()) {
            long end = System.currentTimeMillis();
            Stack<TraceLog> stack = TracingContext.getStack();
            if (!stack.isEmpty()) {
                pop = stack.pop();
                TracingContext.logger.trace("弹出，{}", pop);
                pop.setHandleMills(end - pop.getStartTime());
                pop.setEndTime(end);
                if (pop.getSpanId().equals(pop.getTraceId())) {
                    pop.setRoot(true);
                }
                pop.setLevel(level);
            }
            ;
        } else {
            pop = TraceLog.empty;
            pop.setLevel(level);
        }
        return pop;
    }


}
