package com.griddynamics.logtool;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class AppenderForTesting extends AppenderSkeleton {

    private static LoggingEvent lastMessage = null;

    private String application;

    
    protected void append(LoggingEvent event) {
        if (application != null) {
            event.setProperty("application", application);
        }
        lastMessage = event;
    }

    public void close() {}

    public boolean requiresLayout() {return false;}

    public static LoggingEvent getLastMessage() {
        return lastMessage;
    }

    public static void clear() {
        lastMessage = null;
    }

    public void setApplication(String lapp) {
        this.application = lapp;
    }

    public String getApplication() {
        return application;
    }

    
}