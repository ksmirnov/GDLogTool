package com.griddynamics.logtool;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

public class SyslogServer {
    private int port;
    private SyslogServerHandler syslogServerHandler;
    private final ChannelGroup allChannels = new DefaultChannelGroup("syslog-server");
    private int bindChannelId;
    private final String regexp;
    private final Map<String, Integer> groups;

    public SyslogServer(int port, final String regexp, final Map<String, Integer> groups, Storage storage, SearchServer searchServer) {
        syslogServerHandler = new SyslogServerHandler(storage, searchServer, regexp, groups, allChannels);
        this.regexp = regexp;
        this.groups = groups;
        this.port = port;
    }

    public String getRegexp() {
        return regexp;
    }

    public Map<String, Integer> getGroups() {
        return groups;
    }

    public void initialize() {
        ChannelFactory syslogChanelFactory =
                new NioDatagramChannelFactory(
                        Executors.newCachedThreadPool());

        ConnectionlessBootstrap syslogServerBootstrap = new ConnectionlessBootstrap(syslogChanelFactory);

        syslogServerBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(syslogServerHandler);
            }
        });
        syslogServerBootstrap.setOption("child.keepAlive", true);
        Channel channel = syslogServerBootstrap.bind(new InetSocketAddress(port));
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
