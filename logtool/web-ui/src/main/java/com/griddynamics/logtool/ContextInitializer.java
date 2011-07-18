package com.griddynamics.logtool;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by IntelliJ IDEA.
 * User: slivotov
 * Date: Jul 13, 2011
 * Time: 3:09:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContextInitializer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Consumer.main(new String[0]);

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
