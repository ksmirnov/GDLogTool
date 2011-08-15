package com.griddynamics.logtool;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Log4jEventsServer {
    private int port;
    private final Log4jEventsHandler log4jEventsHandler;
    private final ChannelGroup allChannels = new DefaultChannelGroup("log4j-server");
    private int bindChannelId;

    public Log4jEventsServer(int port, Storage storage, SearchServer searchServer) {
        log4jEventsHandler = new Log4jEventsHandler(storage, searchServer, allChannels);
        this.port = port;
    }

    public void intitialize() {
        Executor threadPool = Executors.newCachedThreadPool();
        ChannelFactory factory = new NioServerSocketChannelFactory(threadPool, threadPool);
        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new LogEventDecoder(),
                        log4jEventsHandler);
            }
        });

        bootstrap.setOption("child.keepAlive", true);
        Channel channel = bootstrap.bind(new InetSocketAddress(port));
        bindChannelId = channel.getId();
        allChannels.add(channel);
    }

    public void shutdown() {
        ChannelFactory factory = allChannels.find(bindChannelId).getFactory();
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
        factory.releaseExternalResources();
    }
}
