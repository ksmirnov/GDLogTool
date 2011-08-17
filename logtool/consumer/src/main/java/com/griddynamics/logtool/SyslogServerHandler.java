package com.griddynamics.logtool;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class SyslogServerHandler extends SimpleChannelHandler {
    private static final Logger logger = LoggerFactory.getLogger(Log4jEventsHandler.class);

    private final Storage storage;
    private final SearchServer searchServer;
    private final MessageParser messageParser;
    private ChannelGroup allChannels;

    public SyslogServerHandler(Storage storage, SearchServer searchServer,
                               String regexp, Map<String,Integer> groups, ChannelGroup allChannels) {
        this.storage = storage;
        this.searchServer = searchServer;
        this.allChannels = allChannels;
        this.messageParser = new MessageParser(regexp,groups);

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
        Map<String,String> msg = messageParser.parseMessage(receivedMessage.toString());

        if(msg.get("content") == null){
            msg.put("content", receivedMessage.toString());
        }
        String [] path = new String[3];
        path[0] = msg.get("application");
        path[1] = host;
        path[2] = msg.get("instance");
        msg.putAll(storage.addMessage(path, msg.get("date"), msg.get("content")));

        Set<String> wipedFiles = storage.getWipedFiles();
        if (!wipedFiles.isEmpty()) {
            for (String logPath : wipedFiles) {
                searchServer.delete("path:" + logPath);
            }
        }

        searchServer.index(msg);
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
    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        allChannels.add(e.getChannel());
    }
}
