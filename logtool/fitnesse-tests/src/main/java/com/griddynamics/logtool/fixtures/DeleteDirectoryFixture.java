package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;

import java.util.HashMap;
import java.util.Map;

public class DeleteDirectoryFixture extends DoFixture {
    private String host;
    private int port;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void deleteDirectory(String path) throws Exception {
        String reversedPath = PathConstructor.reversePath(path);

        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "deleteDirectory");
        params.put("path", reversedPath);
        lr.get(params);
    }

    public boolean checkRemovalOfLogWithApplicationHostInstance(String application, String host, String instance) throws Exception {
        DeleteLogFixture dlf = new DeleteLogFixture();
        dlf.setHost(this.host);
        dlf.setPort(this.port);
        return dlf.checkForExistingLog(application, host, instance);
    }
}