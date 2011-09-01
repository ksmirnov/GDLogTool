package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.Map;


public class SolrSearchFixture extends DoFixture {

    private String host;
    private int port;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean checkSearchWithRequestHaveFinding(String searchQuery, int needToFind) throws Exception {
        LogtoolRequester requester = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "doSolrSearch");
        params.put("subaction", "solrsearch");
        params.put("query", searchQuery);
        String response = requester.get(params);
        response = response.substring(response.indexOf("["), response.length());
        Object obj = JSONValue.parse(response);
        JSONArray array=(JSONArray)obj;
        return array.size() == needToFind;
    }
    
}
