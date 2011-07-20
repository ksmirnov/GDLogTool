package com.griddynamics.logtool;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Consumer.startServer((short)-1);        
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
