package com.griddynamics.logtool;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class SearchServerImpl implements SearchServer {

    private static final Logger logger = LoggerFactory.getLogger(SearchServerImpl.class);

    private EmbeddedSolrServer server;
    private String solrPath;
    

    public SearchServerImpl(String solrPath) {
        if(!solrPath.isEmpty() && !solrPath.equals("default")) {
            File f = new File(solrPath);
            if(f.exists() && f.isDirectory()) {
                System.setProperty("solr.solr.home", solrPath);
            }
        }
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        CoreContainer container;
        try {
            container = initializer.initialize();
        } catch (Exception e) {
            logger.error("Unable to initialize solr");
            return;
        }
        this.solrPath = container.getSolrHome();
        server = new EmbeddedSolrServer(container, "collection1");
    }

    @Override
    public void index(Map<String, String> fields) throws IllegalArgumentException {
        if(!fields.containsKey("path") || !fields.containsKey("startIndex") || !fields.containsKey("length")) {
            throw new IllegalArgumentException("Missing one or more required fields");
        }
        SolrInputDocument doc = new  SolrInputDocument();
        Set<String> keySet = fields.keySet();
        for(String key : keySet) {
            doc.addField(key, fields.get(key));
        }
        try {
            server.add(doc);
            server.commit();
        } catch (Exception e) {
            logger.error("Unable to index document", e);
        }
    }

    @Override
    public List<Map<String, String>> search(String query) {
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setRows(Integer.MAX_VALUE);
        QueryResponse resp;
        try {
            resp = server.query(solrQuery);
        } catch (Exception e) {
            logger.error("Search query failed", e);
            return null;
        }
        SolrDocumentList docs = resp.getResults();
        List out = new ArrayList();
        if(docs != null) {
            for(SolrDocument doc : docs) {
                Map<String, String> entry = new HashMap<String, String>();
                for(String name : doc.getFieldNames()) {
                    entry.put(name, (String) doc.getFieldValue(name));
                }
                out.add(entry);
            }
        }
        return out;
    }

    @Override
    public void delete(String query) {
        try {
            server.deleteByQuery(query);
            server.commit();
        } catch (Exception e) {
            logger.error("Delete query failed", e);
        }
    }

    public String getSolrPath() {
        return solrPath;
    }

}