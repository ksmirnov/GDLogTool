package com.griddynamics.logtool;

import java.util.List;
import java.util.Map;

/**
 * API for search server 
 */
public interface SearchServer {

    /**
     * Indexes fields contained in Map<String, String> on search server
     * @param fields - Collection of field names and its values
     */
    void index(Map<String, String> fields);

    /**
     * Retrieves entries from search server by some query
     * @param query - query to retrieve data, depends on search server query syntax
     * @return - List of found entries Map<String, String> (empty if nothing found)
     */
    List<Map<String, String>> search(String query);

    /**
     * Deletes entries from from search server by some query
     * @param query - query to retrieve entries to delete, depends on search server query syntax
     */
    void delete(String query);

    /**
    * Stops server
    */
    void shutdown();

}
