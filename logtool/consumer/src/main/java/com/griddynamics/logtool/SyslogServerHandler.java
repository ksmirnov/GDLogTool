package com.griddynamics.logtool;

import org.apache.log4j.spi.LoggingEvent;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.InetSocketAddress;


public class SyslogServerHandler extends SimpleChannelHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerHandler.class);

    private final Storage storage;

    public SyslogServerHandler(Storage storage) {
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
        ChannelBuffer buf = (ChannelBuffer) e.getMessage();
        StringBuilder receivedMessage = new StringBuilder("");
        while (buf.readable()) {
            receivedMessage.append((char) buf.readByte());
        }
        ParsedMessage msg = MessageParser.parseMessage(receivedMessage.toString());
        String [] path = new String [3];
        path[0] = msg.getApplication();
        path[1] = host;
        path[2] = msg.getInstance();;
        storage.addMessage(path, msg.getTimestamp(), msg.getMessage());
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
