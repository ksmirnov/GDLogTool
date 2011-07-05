package com.griddynamics.logtool;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Storage {
    void setLogFolder(String logFolder);

    void setMaxLogFolderLength(long length);

    void saveLog(String application, String host, String instance, Date date, String message) throws IOException;

    List<String> getLog(String application, String host, String instance, Date date) throws IOException;

    void deleteLog(String application, String host, String instance, Date date) throws IOException;

    Map<String, Object> getTree();

    String[] getSubTree(String application, String host, String instance);

    long getLastUpdateTime();
}
