package com.xl.tracer.log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tanjl11
 * @date 2021/12/20 10:28
 */
public class LogSenderFactory {

    public static Map<String, LogSender> senderMap = new ConcurrentHashMap<>();

    public static LogSender getEsSender() {
        LogSender logSender = senderMap.get(EsLogSender.NAME);
        if (logSender == null) {
            logSender = new EsLogSender();
            senderMap.put(EsLogSender.NAME, logSender);
        }
        return logSender;
    }
}
