package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class GetLogFixture extends DoFixture {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormat.forPattern("yyyy-dd-MMM").withLocale(Locale.ENGLISH);

    private String host;
    private int port;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean checkLogFromAppOnWithInstanceContain
            (String application, String host, String instance, String message) throws Exception {
        String fs = System.getProperty("file.separator");
        LogtoolRequester requester = new LogtoolRequester(this.host, port);
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "getLog");
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(FORMATTER.print(System.currentTimeMillis())).append(".log").append(fs);
        pathBuilder.append(instance).append(fs);
        pathBuilder.append(host).append(fs);
        pathBuilder.append(application);
        params.put("path", pathBuilder.toString());
        params.put("partToView", "-1");
        params.put("lines", "2500");
        String response = requester.get(params);
        int index = response.indexOf(message);
        return index > 0;
    }
}
