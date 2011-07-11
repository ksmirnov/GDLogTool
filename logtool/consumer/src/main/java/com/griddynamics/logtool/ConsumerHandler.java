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

import java.util.StringTokenizer;

public class ConsumerHandler extends SimpleChannelHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerHandler.class);

    private final static String EMPTY = "(default)";
    private final static String DELIM = ".";

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
        if (e.getMessage() instanceof LoggingEvent) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
            String message = ((LoggingEvent) e.getMessage()).getMessage().toString();
            String[] entry = parseMessage(e);
            DateTime date = new DateTime(((LoggingEvent) e.getMessage()).timeStamp);
            String timestamp = date.toString(dateTimeFormatter);
            storage.addMessage(entry, timestamp, message);
        } else throw new IllegalArgumentException("argument is not instance of LoggingEvent");
    }

    /**
     * Method to parse recieved object
     * @throws IllegalArgumentException
     */
    protected String[] parseMessage(MessageEvent e) {
        String[] out = new String[3];
        if (e.getMessage() instanceof LoggingEvent) {
            LoggingEvent loggingEvent = (LoggingEvent) e.getMessage();
            String app = loggingEvent.getProperty("application");
            if (app != null && app.length() > 0) {
                StringTokenizer st = new StringTokenizer(app, DELIM);
                if(st.countTokens() <= 2) {
                    out[0] = st.nextToken();
                    if (st.hasMoreTokens()) out[2] = st.nextToken();
                    else out[2] = EMPTY;
                } else throw new IllegalArgumentException("illegal application / instance");
            } else out[0] = out[2] = EMPTY;
            out[1] = e.getRemoteAddress().toString();
            out[1] = out[1].substring(2, out[1].indexOf(':'));
        } else throw new IllegalArgumentException("argument is not instance of LoggingEvent");
        return out;
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
