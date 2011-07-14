package com.griddynamics.logtool;

import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.spi.LoggingEvent;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConsumerHandlerTest {
    private static final Logger testLogger = Logger.getLogger(ConsumerHandlerTest.class);

    @BeforeClass
    public static void doBefore() {
        AppenderForTesting.clear();
        testLogger.info("THIS IS TEST MESSAGE");
    }

    @Test
    public void testMessageReceived() {
        final LoggingEvent testEvent = AppenderForTesting.getLastMessage();
        Storage mockedStorage = mock(Storage.class);
        ConsumerHandler handlerInstance = new ConsumerHandler(mockedStorage);
        String [] strArray = new String[3];
        MessageEvent testMessage = new MessageEvent() {

            @Override
            public Object getMessage() {
                return testEvent;
            }

            @Override
            public SocketAddress getRemoteAddress() {
                return new InetSocketAddress(4444);
            }

            @Override
            public Channel getChannel() {
                return null;
            }

            @Override
            public ChannelFuture getFuture() {
                return null;
            }
        };
        try {
            handlerInstance.messageReceived(null, testMessage);
            verify(mockedStorage).addMessage(any(strArray.getClass()), anyString(), eq("THIS IS TEST MESSAGE"));
        } catch (IllegalArgumentException e) {
            fail("Illegal argument");
        }

    }

    @Test
    public void testGetApplication() {
        final LoggingEvent testEvent = AppenderForTesting.getLastMessage();
        Storage mockedStorage = mock(Storage.class);
        ConsumerHandler handlerInstance = new ConsumerHandler(mockedStorage);
        assertEquals(handlerInstance.getApplication(testEvent), "testApp");
    }

    @Test
    public void testGetInstance() {
        final LoggingEvent testEvent = AppenderForTesting.getLastMessage();
        Storage mockedStorage = mock(Storage.class);
        ConsumerHandler handlerInstance = new ConsumerHandler(mockedStorage);
        assertEquals(handlerInstance.getInstance(testEvent), "testInstance");
    }
}
