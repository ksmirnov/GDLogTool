package com.griddynamics.logtool;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Storage {
    void setLogFolder(String logFolder);

    void setMaxFolderSize(long length);

    void saveLog(String application, String host, String instance, Date date, String message);

    List<String> getLog(String application, String host, String instance, Date date);

    void deleteLog(String application, String host, String instance, Date date);

    void deleteLog(String application, String host, String instance);

    void deleteLog(String application, String host);

    void deleteLog(String application);

    Map<String, Object> getTree();

    String[] getSubTree(String application, String host, String instance);

    long getLastUpdateTime();

    void initialize();
}
