package com.griddynamics.logtool;

import org.jboss.netty.channel.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HandlerMonitor extends SimpleChannelHandler {

    SimpleChannelHandler handler;
    private Map<String, ChannelPerformance> statement = new HashMap<String, ChannelPerformance>();

    public HandlerMonitor(SimpleChannelHandler handler) {
        this.handler = handler;
    }

    public String buildKey(ChannelHandlerContext ctx) {
        return ctx.getChannel().getClass().getSimpleName() + ", id = " + ctx.getChannel().getId();
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        handler.channelOpen(ctx, e);
    }

    public ChannelPerformance addChannel(String key) {
        synchronized(statement) {
            statement.put(key, new ChannelPerformance());
        }
        return statement.get(key);
    }

    public void reset() {
        synchronized(statement) {
            statement = new HashMap<String, ChannelPerformance>();
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelPerformance perf = statement.get(buildKey(ctx));
        if(perf == null) {
            perf = addChannel(buildKey(ctx));
        }
        int receivedBefore = perf.getRecieved();
        if(e.getMessage() instanceof List) {
            perf.addRecieved(((List)e.getMessage()).size());
        } else {
            perf.addRecieved(1);
        }
        if(perf.getFirstReceived() == 0) {
            perf.setFirstReceived(System.currentTimeMillis());
        }
        long initTime = System.nanoTime();
        handler.messageReceived(ctx, e);
        long endTime = System.nanoTime();
        perf.setAverageLatency(
                (
                        perf.getAverageLatency() * receivedBefore + endTime - initTime
                ) / perf.getRecieved()
        );
        perf.setLastRecieved(System.currentTimeMillis());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        ChannelPerformance perf = statement.get(buildKey(ctx));
        if(perf == null) {
            perf = addChannel(buildKey(ctx));
        }
        perf.addException();
        handler.exceptionCaught(ctx, e);
    }

    public Map<String, ChannelPerformance> getPerformance() {
        Map<String, ChannelPerformance> out = new HashMap(statement);
        return out;
    }
}
