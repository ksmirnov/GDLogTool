package com.griddynamics.logtool.fixtures;

import com.griddynamics.logtool.agents.UDPSendler;
import fitlibrary.DoFixture;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;


public class SendlerFixture extends DoFixture {

    private String host;
    private int port;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void sendMessageFromApplicationThroughTCPInNumberOfWithDelay
            (String message, String application, int amount, long delay)throws Exception {
        Logger logger = Logger.getLogger(SendlerFixture.class);
        SocketAppender appender = new SocketAppender(host, port);
        appender.setApplication(application);
        logger.addAppender(appender);
        for(int i = 0; i < amount; i ++) {
            logger.info(message + " " + i);
            Thread.sleep(delay);
        }
    }
    
    public void sendMessageThroughUDPInNumberOfWithDelay(String message, int amount, long delay) throws Exception {
        UDPSendler sendler = new UDPSendler(host, port);
        for(int i = 0; i < amount; i ++) {
            sendler.sendMsg(message + " " + i);
            Thread.sleep(delay);
        }
    }
}
