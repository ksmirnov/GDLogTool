package com.griddynamics.logtool;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.InetSocketAddress;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Consumer {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private static final short DEF_PORT = 4444;
    private static final short SYSLOG_DEF_PORT = 4445;


    public static void main(String[] args) {
            boolean startServerForSocketAppender = true;
            boolean startServerForSyslogAppender = true; 

        short port = DEF_PORT;
        if(args.length == 1) {
            try {
                port = Short.parseShort(args[1]);
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
            }
        }
        BeanFactory springFactory = new ClassPathXmlApplicationContext("fileStorageConfiguration.xml");
        final FileStorage fileStorage = (FileStorage) springFactory.getBean("fileStorage");

        if (startServerForSocketAppender) {
            Executor threadPool = Executors.newCachedThreadPool();
            ChannelFactory factory = new NioServerSocketChannelFactory(threadPool, threadPool);
            ServerBootstrap bootstrap = new ServerBootstrap(factory);
            final ConsumerHandler consumerHandler = new ConsumerHandler(fileStorage);

            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() {
                    return Channels.pipeline(
                            new LogEventDecoder(),
                            consumerHandler);
                }
            });

            bootstrap.setOption("child.keepAlive", true);
            bootstrap.bind(new InetSocketAddress(port));
        }

        if (startServerForSyslogAppender) {
            ChannelFactory syslogChanelFactory =
                    new NioDatagramChannelFactory(
                            Executors.newCachedThreadPool());

            ConnectionlessBootstrap syslogServerBootstrap = new ConnectionlessBootstrap(syslogChanelFactory);

            syslogServerBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() {
                    return Channels.pipeline(new SyslogServerHandler(fileStorage));
                }
            });
            syslogServerBootstrap.setOption("child.keepAlive", true);
            syslogServerBootstrap.bind(new InetSocketAddress(SYSLOG_DEF_PORT));
        }

    }
    public static void startServer(short socketServerPort){
        if(socketServerPort != -1){
            String[] socketServerPortSting = {Short.toString(socketServerPort)};

            main(socketServerPortSting);
        } else {
            main(new String[0]);
        }

    }
}
