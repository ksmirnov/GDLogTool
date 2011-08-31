package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeleteLogFixture extends DoFixture {
    public boolean atHostWithPortDeleteLogInWithFileName(String host, int port, String path, String fileName) throws Exception {
        String reversedPath = PathConstructor.reversePath(path);
        String pathToDelete = fileName + "/" + reversedPath;

        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "getLog");
        params.put("path", pathToDelete);
        lr.get(params);

        params.clear();
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
