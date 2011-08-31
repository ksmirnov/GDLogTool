package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteDirectoryFixture extends DoFixture {
    
    public void atHostWithPortDeleteDirectory(String host, int port, String path) throws Exception {
        String reversedPath = com.griddynamics.logtool.fixtures.PathConstructor.reversePath(path);

        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "deleteDirectory");
        params.put("path", reversedPath);
        lr.get(params);
    }

    public boolean atHostWithPortCheckRemovalOfLogWithPathAndFileName(String host, int port, String path, String fileName) throws Exception {
        String reversedPath = com.griddynamics.logtool.fixtures.PathConstructor.reversePath(path);
        String pathToDelete = fileName + "/" + reversedPath;

        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "deleteLog");
        params.put("path", pathToDelete);
        params.put("partToView", "-1");
        params.put("lines", "2500");
        String getLogResponse = lr.get(params);

        params.clear();
        params.put("action", "doSolrSearch");
        params.put("subaction", "solrsearch");
        List<String> pathSegments = PathConstructor.getPathSegments(path);
        String query = "application:" + pathSegments.get(0) + " host:" + pathSegments.get(1) + " instance:" + pathSegments.get(2);
        params.put("query", query);
        String solrSearchResponse = lr.get(params);

        return getLogResponse.equals("response =") && solrSearchResponse.equals("occurrences = []");
    }
}