package com.griddynamics.logtool;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Consumer {

    private int log4jPort = 4444;
    private int syslogPort = 4445;
    private Storage storage;
    private SearchServer searchServer;

    public void setLog4jPort(int log4jPort) {
        this.log4jPort = log4jPort;
    }

    public void setSyslogPort(int syslogPort) {
        this.syslogPort = syslogPort;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setSearchServer(SearchServer searchServer) {
        this.searchServer = searchServer;
    }

    public void startLog4j() {
        Executor threadPool = Executors.newCachedThreadPool();
        ChannelFactory factory = new NioServerSocketChannelFactory(threadPool, threadPool);
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        final ConsumerHandler consumerHandler = new ConsumerHandler(storage, searchServer);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new LogEventDecoder(),
                        consumerHandler);
            }
        });

        bootstrap.setOption("child.keepAlive", true);
        bootstrap.bind(new InetSocketAddress(log4jPort));
    }

    public void startSyslog() {
        ChannelFactory syslogChanelFactory =
                new NioDatagramChannelFactory(
                        Executors.newCachedThreadPool());

        ConnectionlessBootstrap syslogServerBootstrap = new ConnectionlessBootstrap(syslogChanelFactory);

        syslogServerBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new SyslogServerHandler(storage, searchServer));
            }
        });
        syslogServerBootstrap.setOption("child.keepAlive", true);
        syslogServerBootstrap.bind(new InetSocketAddress(syslogPort));
    }
    public void startServers() {
        startLog4j();
        startSyslog();
    }
}
