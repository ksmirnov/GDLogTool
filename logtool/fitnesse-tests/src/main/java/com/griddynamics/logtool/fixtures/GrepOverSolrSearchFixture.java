package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class GrepOverSolrSearchFixture extends DoFixture{
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
        params.put("subaction", "grepOverSolr");
        params.put("query", searchQuery.substring(0, searchQuery.indexOf("grep:")-1));
        params.put("request", searchQuery.substring(searchQuery.indexOf("grep:")+6, searchQuery.length()));
        params.put("pageSize", "2500");
        String response = requester.get(params);
        StringTokenizer st = new StringTokenizer(response,"||<>||");
        int counter = -1;
        while (st.hasMoreElements()){
                    st.nextToken();
            counter++;
        }
        return counter == needToFind;
    }
}
