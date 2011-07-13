package com.griddynamics.logtool;

import org.apache.log4j.spi.LoggingEvent;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.net.InetSocketAddress;
import java.util.StringTokenizer;

public class ConsumerHandler extends SimpleChannelHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerHandler.class);

    private static final String DELIM = ".";

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    private final Storage storage;

    public ConsumerHandler(Storage storage) {
        this.storage = storage;
    }

    /**
     * Invoked when a message object (e.g: {@link org.jboss.netty.buffer.ChannelBuffer}) was received
     * from a remote peer.
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws IllegalArgumentException {
        String host;
        if(e.getRemoteAddress() instanceof InetSocketAddress) {
            host = ((InetSocketAddress) e.getRemoteAddress()).getHostName();
        } else {
            host = e.getRemoteAddress().toString();
        }
        if(e.getMessage() instanceof LoggingEvent) {
            LoggingEvent loggingEvent = (LoggingEvent) e.getMessage();
            String message = loggingEvent.getMessage().toString();
            DateTime date = new DateTime(loggingEvent.timeStamp);
            String timestamp = date.toString(dateTimeFormatter);
            String[] entry = new String[3];
            entry[0] = getApplication(loggingEvent);
            entry[1] = host;
            entry[2] = getInstance(loggingEvent);
            storage.addMessage(entry, timestamp, message);
        } else {
            throw new IllegalArgumentException("argument is not instance of LoggingEvent");
        }
    }

    /**
     * Gets application name from log4j logging event
     * @param event the log4j LoggingEvent
     * @return the string containing application name
     */
    protected String getApplication(LoggingEvent event) {
        String out = event.getProperty("application");
        if(out.length() > 1 && out.indexOf(DELIM) > 0) {
            out = out.substring(0, out.indexOf(DELIM));
        }
        return out;
    }

     /**
     * Gets application instance name from log4j logging event
     * @param event the log4j LoggingEvent
     * @return the string containing application instance name
     */
    protected String getInstance(LoggingEvent event) {
        String out = event.getProperty("application");
        if(out.length() > 1 && out.indexOf(DELIM) > 0) {
            out = out.substring(out.indexOf(DELIM) + 1);
            return out;
        } else {
            return null;
        }
    }

    /**
     * Invoked when an exception was raised
     * {@link org.jboss.netty.channel.ChannelHandler}.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {

        logger.error(e.getCause().getMessage(), e.getCause());

        Channel ch = e.getChannel();
        ch.close();
    }
}
