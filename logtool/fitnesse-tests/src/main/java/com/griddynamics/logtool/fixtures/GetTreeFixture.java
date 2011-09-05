package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class GetTreeFixture extends DoFixture {

    private String host;
    private int port;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isPathInTheTree(String path) throws Exception {
        String fs = System.getProperty("file.separator");
        LogtoolRequester requester = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "getTree");
        String response = requester.get(params);
        StringTokenizer tokenizer = new StringTokenizer(path, fs);
        boolean out = false;
        while(tokenizer.hasMoreTokens()) {
            out = response.indexOf(tokenizer.nextToken()) > 0;
            if(!out) {
                return false;
            }
        }
        return out;
    }
}
