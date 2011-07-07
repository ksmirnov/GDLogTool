package com.griddynamics.logtool;

import java.util.List;

/**
 * Storage interface provides to Consumer possibility to add log message to
 * log storage and to Web-UI - get, delete logs and monitoring storage structure
 */
public interface Storage {
    /**
     * This method adds one log message to an existing log file or creates
     * appropriate log file and prints in it
     * @param path - Array of path separators
     * @param timestamp - Time of log message
     * @param message - Log message
     */
    void addMessage(String[] path, String timestamp, String message);

    /**
     * This method reads text of appropriate log file and returns it as List<String>
     * @param path - Array of path separators
     * @param logName - Log file name
     * @return - Log file text divided by lines and presented as List<String>
     */
    List<String> getLog(String[] path, String logName);

    /**
     * This method deletes appropriate log file (or files if array has more than
     * one element, or entirely all folder if array is empty)
     * @param path - Array of path separators
     * @param names - Array of log file names
     */
    void deleteLog(String[] path, String ... names);

    /**
     * This method returns storage structure for servlet's UI.
     * If height equals -1 and path is empty it returns all storage structure.
     * @param path - Array of path separators which navigates
     * to necessary node of storage structure
     * @param height - Height indicates how deep to take subtree
     * @return - Appropriate subtree of storage structure
     */
    Tree getTree(int height, String ... path);

    /**
     * This method return last storage update time
     * (last time of adding or deleting smth)
     * @return - Last storage update time
     */
    long getLastUpdateTime();
}
