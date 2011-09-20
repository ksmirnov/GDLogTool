package com.griddynamics.logtool;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @param start - start index in search result
     * @param amount - amount of interesting entries
     * @param sortField - field to sort by
     * @param order - order (ascending/descending)
     * @return - List of found entries Map<String, String> (empty if nothing found)
     */
    List<Map<String, String>> search(String query, int start, int amount, String sortField, String order);

    /**
     * Returns set of facets given by filter query
     * @param query - filter query, depends on search server query syntax
     * @return - Set of facets (empty if nothing found, null if query is wrong)
     */
    Set<Facet> getFacets(String query);

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
