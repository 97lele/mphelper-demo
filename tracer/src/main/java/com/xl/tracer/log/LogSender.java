package com.xl.tracer.log;

import java.util.Collection;

/**
 * @author tanjl11
 * @date 2021/12/20 9:58
 */
public interface LogSender {

    String name();

    void batchSend(Collection<TraceLog> log);

    void singleSend(TraceLog log);
}
