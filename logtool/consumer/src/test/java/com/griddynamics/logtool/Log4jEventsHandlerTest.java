package com.griddynamics.logtool;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;


public class Log4jEventsHandlerTest {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
    private final DateTimeFormatter indexFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private Storage mockedStorage;
    private SearchServer mockedSearch;
    private Log4jEventsHandler testHandler;
    private ChannelGroup mockedChannelGroup;  
    

    @Before
    public void init() {
        mockedStorage = mock(Storage.class);
        mockedSearch = mock(SearchServer.class);
        mockedChannelGroup = mock(ChannelGroup.class);
        testHandler = new Log4jEventsHandler(mockedStorage, mockedSearch, mockedChannelGroup);
    }

    @Test
    public void testMessageRecieved() {
        long datetime = System.currentTimeMillis();
        DateTime date = new DateTime(datetime);
        String timestamp = date.toString(dateTimeFormatter);
        String message = "Test message";

        Category logger = mock(Category.class);
        LoggingEvent testEvent = new LoggingEvent(null, logger, datetime, Level.INFO, message, null);
        testEvent.setProperty("application", "testApp.testInstance");
        List testEventsList = new ArrayList();
        testEventsList.add(testEvent);

        InetSocketAddress testAddress = new InetSocketAddress("localhost", 4444);
        System.out.println(testAddress.getAddress());

        ChannelHandlerContext testCtx = mock(ChannelHandlerContext.class);
        Channel testChannel = mock(Channel.class);
        when(testChannel.getLocalAddress()).thenReturn(testAddress);
        when(testCtx.getChannel()).thenReturn(testChannel);

        MessageEvent testMessage = mock(MessageEvent.class);
        when(testMessage.getRemoteAddress()).thenReturn(testAddress);
        when(testMessage.getMessage()).thenReturn(testEventsList);

        testHandler.messageReceived(testCtx, testMessage);

        String[] pathToVerify = new String[3];
        pathToVerify[0] = "testApp";
        pathToVerify[1] = "localhost";
        pathToVerify[2] = "testInstance";
        message = timeFormatter.print(datetime) + " " + message;
        verify(mockedStorage).addMessage(pathToVerify, timestamp, message);

        Map<String, String> mapToVerify = new LinkedHashMap<String, String>();
        mapToVerify.put("application", pathToVerify[0]);
        mapToVerify.put("host", pathToVerify[1]);
        mapToVerify.put("instance", pathToVerify[2]);
        mapToVerify.put("content", message);
        mapToVerify.put("timestamp", indexFormatter.print(datetime));
        mapToVerify.put("level", Level.INFO.toString());
        mapToVerify.put("port", "4444");
        verify(mockedSearch).index(mapToVerify);
    }
}
