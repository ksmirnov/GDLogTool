package com.griddynamics.logtool.fixtures;

import com.griddynamics.logtool.agents.UDPSendler;
import fitlibrary.DoFixture;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class SendlerFixture extends DoFixture {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS");

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
        appender.close();
    }
    
    public void sendMessageFromApplicationWithInstanceThroughUDPInNumberOfWithDelay
            (String message, String application, String instance, int amount, long delay) throws Exception {
        UDPSendler sendler = new UDPSendler(host, port);
        StringBuilder sb = new StringBuilder();
        String delim = "|";
        sb.append(delim).append(application);
        sb.append(delim).append(instance);
        sb.append(delim).append(dateTimeFormatter.print(System.currentTimeMillis()));
        sb.append(delim).append(message);
        message = sb.toString();
        for(int i = 0; i < amount; i ++) {
            sendler.sendMsg(message + " " + i);
            Thread.sleep(delay);
        }
    }
}
