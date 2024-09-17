package com.additionaltools.common;


import com.additionaltools.logging.LoggingService;

public class EmptyLoggingService implements LoggingService {
    public void addLog(String log) {
        //do nothing
    }

    public void flushLogs() {
        //do nothing
    }
}