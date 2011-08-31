package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;

import java.util.HashMap;
import java.util.Map;

public class DeleteLogFixture extends DoFixture {
    private String host;
    private int port;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean deleteLogFromApplicationHostInstance(String application, String host, String instance) throws Exception {
        String pathToDelete = PathConstructor.createPath(application, host, instance);

        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "deleteLog");
        params.put("path", pathToDelete);
        lr.get(params);

        return checkForExistingLog(application, host, instance);
    }

    public boolean checkForExistingLog(String application, String host, String instance) throws Exception {
        String pathToDelete = PathConstructor.createPath(application, host, instance);

        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "getLog");
        params.put("path", pathToDelete);
        params.put("partToView", "-1");
        params.put("lines", "2500");
        String getLogResponse = lr.get(params);

        params.clear();
        params.put("action", "doSolrSearch");
        params.put("subaction", "solrsearch");
        String query = "application:" + application + " host:" + host + " instance:" + instance;
        params.put("query", query);
        String solrSearchResponse = lr.get(params);

        return getLogResponse.equals("response = ") && solrSearchResponse.equals("occurrences = []");
    }
}
