package com.griddynamics.logtool;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Storage interface provides to Consumer possibility to add log message to
 * log storage and to Web-UI - get, delete logs and monitoring storage structure
 */
public interface Storage {
    /**
     * This method adds one log message to an existing log file or creates
     * appropriate log file and prints in it
     * @param path - Array of path segments
     * @param timestamp - Time of log message
     * @param message - Log message
     * @return - Collection containing information about stored message
     */
    Map<String, String> addMessage(String[] path, String timestamp, String message);

    /**
     * This method reads text of appropriate log file and returns it as List<String>
     * @param path - Array of path segments
     * @param logName - Log file name
     * @return - Log file text divided by lines and presented as List<String>
     */
    List<String> getLog(String[] path, String logName);

    /**
     * This method deletes appropriate log file.
     * @param path - Array of path segments
     * @param name - Log file name
     * @return - Deleted file name
     */
    String deleteLog(String[] path, String name);

    /**
     * This method deletes appropriate log folder.
     * @param path - Array of path segments
     * @return - Collection of deleted files names
     */
    Set<String> deleteDirectory(String path[]);

    /**
     * This method returns storage structure for servlet's UI.
     * If height equals -1 and path is empty it returns all storage structure.
     * @param path - Array of path segments which navigates
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

    /**
     * This method subscribes user for alerts of messages satisfying specified
     * filter recorded in the form of regular expression.
     * @param filter - filter for alerts
     * @param emailAddress - email address to which to send alerts
     */
    void subscribe(String filter, String emailAddress);

    /**
     * This method unsubscribes user for alerts of messages satisfying specified
     * filter recorded in the form of regular expression.
     * @param filter - filter for alerts
     * @param emailAddress - email address to which to send alerts
     */
    void unsubscribe(String filter, String emailAddress);

    /**
     * This method returns all current subscribers for all current filter
     * represented as map with filter as key and HashSet's of users emails as values.
     * @return - all current subscribers for all current filter
     */
    Map<String, HashSet<String>> getSubscribers();

    /**
     * This method subscribes user for storage quota reached alerts.
     * @param emailAddress - email address to which to send alerts
     */
    void subscribeToQuotaAlert(String emailAddress);

    /**
     * This method unsubscribes user for storage quota reached alerts.
     * @param emailAddress - email address to which to send alerts
     */
    void unsubscribeToQuotaAlert(String emailAddress);

    /**
     * This method removes from file storage alert filter
     * recorded in the form of regular expression.
     * @param filter - filter for alerts
     */
    void removeFilter(String filter);

    /**
     * This method returns all alerts occurred after starting service
     * @return - all occurred alerts represented as map with filters as keys
     * and all messages as HashSet value.
     */
    Map<String, HashSet<String>> getAlerts();

    /**
     * This method removes specified alert from file storage.
     * @param filter - triggered filter
     * @param message - message which satisfied filter
     */
    void removeAlert(String filter, String message);

    /**
     * This method searches for request string in all log files which lie
     * in a specified folder and its subfolders.
     * @param path - Array of path segments
     * @param request - String to search
     * @param pageSize - Size of page for read
     * @return - All occurrences of request represented as HashMap
     * with absolute log file name as key and HashMap with number
     * of chunk of file in which request was found as key
     * and request positions in that chunk represented as ArrayList as value
     * as value. (Or as HashMap with absolute log file name as key and HashMap with number
     * of line of file in which request was found as key
     * and request positions in that line represented as ArrayList as value
     * as value.)
     * @throws IOException - Throws IOException if has some problems with I/O in the search.
     */
    Map<String, Map<Integer, List<Integer>>> doSearch(String[] path, String request, int pageSize) throws IOException;

    /**
     * This method return current length of specified log file.
     * @param path - Array of path segments
     * @param name - Log file name
     * @return - Log file length in bytes
     * @throws IOException - Throws IOException if has some problems with input
     */
    long getLogLength(String[] path, String name) throws IOException;

    /**
     * This method reads specified portion of specified file into stream.
     * @param path - Array of path segments
     * @param name - Log file name
     * @param startPos - Position in file
     * @param length - Number of bytes to read
     * @param outputStream - Stream where to write read file
     * @throws IOException - Throws IOException if has some problems with input
     */
    void getLogNew(String[] path, String name, long startPos, int length, OutputStream outputStream) throws IOException;
}
