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
        return ctx.getChannel().getClass().getName() + ", id = " + ctx.getChannel().getId();
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
        if(e.getMessage() instanceof List) {
            perf.addRecieved(((List)e.getMessage()).size());
        } else {
            perf.addRecieved(1);
        }
        long initTime = System.currentTimeMillis();
        if(perf.getStartTime() == 0) {
            perf.setStartTime(initTime);
        }
        handler.messageReceived(ctx, e);
        long endTime = System.currentTimeMillis();
        perf.setEndTime(endTime);
        perf.setAverageLatency((int)(perf.getAverageLatency() * perf.getRecieved() + endTime - initTime) / (perf.getRecieved() + 1));
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
