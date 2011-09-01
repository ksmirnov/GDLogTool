package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GrepSearchFixture extends DoFixture {
    private String host;
    private int port;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean checkSearchByPathWithRequestHaveFinding(String path, String request, int needToFind) throws Exception {
        if(request.indexOf("grep:") != -1) {
            request = request.substring(request.indexOf("grep:")+6, request.length());
        }
        LogtoolRequester requester = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "doSearch");
        params.put("pageSize", "2500");
        params.put("path", path);
        params.put("searchRequest", request);
        String response = requester.get(params);
        Pattern pattern = Pattern.compile(".+=\\[(.+)\\]\\}.+");
        Matcher matcher = pattern.matcher(response);
        if(matcher.matches()){
            response = matcher.group(1);
        } else {
            return needToFind == 0;
        }
        StringTokenizer st = new StringTokenizer(response,",");
        int counter = 0;
        while (st.hasMoreElements()){
                    st.nextToken();
            counter++;
        }
        return counter == needToFind;
    }
}