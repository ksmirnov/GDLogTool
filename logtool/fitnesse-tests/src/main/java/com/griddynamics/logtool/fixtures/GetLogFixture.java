package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
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

    private String getLog
            (String logApplication, String logHost, String logInstance, String date, int partToView, int count)
            throws Exception{
        String fs = System.getProperty("file.separator");
        LogtoolRequester requester = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "getLog");
        StringBuilder pathBuilder = new StringBuilder();
        if(date != null) {
            pathBuilder.append(FORMATTER.print(FORMATTER.parseDateTime(date))).append(".log").append(fs);
        } else {
            pathBuilder.append(FORMATTER.print(System.currentTimeMillis())).append(".log").append(fs);
        }
        pathBuilder.append(logInstance).append(fs);
        pathBuilder.append(logHost).append(fs);
        pathBuilder.append(logApplication);
        params.put("path", pathBuilder.toString());
        params.put("partToView", String.valueOf(partToView));
        params.put("lines", String.valueOf(count));
        String response = requester.get(params);
        int index = response.indexOf("'log' : '");
        System.out.println(response);
        return response.substring(index + 9, response.length() - 3);
    }

    public boolean checkLogFromPathContainsOnLastPage(String path, String message) throws Exception {
        List<String> segments = PathConstructor.getPathSegments(path);
        if(segments.size() < 3) {
            throw new IllegalArgumentException("Not enough path segments");
        } else {
            return true;
        }
    }
}
