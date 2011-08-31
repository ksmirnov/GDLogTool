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
import java.util.concurrent.atomic.AtomicInteger;

public class SearchServerImpl implements SearchServer {

    private static final Logger logger = LoggerFactory.getLogger(SearchServerImpl.class);

    private CoreContainer container;
    private EmbeddedSolrServer server;
    private String solrPath;
    private int cacheBeforeCommit;
    private AtomicInteger docsCounter = new AtomicInteger(0);

    public SearchServerImpl(String solrPath, int cacheBeforeCommit, long updatePeriod) {
        if(!solrPath.isEmpty() && !solrPath.equals("default")) {
            File f = new File(solrPath);
            if(f.exists() && f.isDirectory()) {
                System.setProperty("solr.solr.home", solrPath);
            }
        }
        this.cacheBeforeCommit = cacheBeforeCommit;
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        try {
            container = initializer.initialize();
        } catch (Exception e) {
            logger.error("Unable to initialize solr");
            return;
        }
        this.solrPath = container.getSolrHome();
        server = new EmbeddedSolrServer(container, "collection1");
        TimerTask commitTask = new TimerTask() {
            public void run() {
                try {
                    server.commit();
                    docsCounter.set(0);
                } catch (Exception e) {
                    logger.error("Unable to commit documents by timer", e);
                }
            }
        };
        Timer timer = new Timer(true);
        timer.schedule(commitTask, updatePeriod, updatePeriod);
    }

    @Override
    public void index(Map<String, String> fields) throws IllegalArgumentException {
        if(!fields.containsKey("path") || !fields.containsKey("startIndex") || !fields.containsKey("length") ||
                !fields.containsKey("content")) {
            throw new IllegalArgumentException("Missing one or more required fields");
        }
        SolrInputDocument doc = new  SolrInputDocument();
        Set<String> keySet = fields.keySet();
        for(String key : keySet) {
            doc.addField(key, fields.get(key));
        }
        try {
            server.add(doc);
            if(docsCounter.incrementAndGet() >= cacheBeforeCommit) {
                server.commit();
                docsCounter.set(0);
            }
        } catch (Exception e) {
            logger.error("Unable to index document", e);
        }
    }

    @Override
    public List<Map<String, String>> search(String query) {
        try {
            server.commit();
            docsCounter.set(0);
        } catch (Exception e) {
            logger.error("Unable to commit documents before perform search query", e);
        }
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
                    entry.put(name, doc.getFieldValue(name).toString());
                }
                out.add(entry);
            }
        }
        return out;
    }

    @Override
    public void delete(String query) {
        try {
            server.commit();
            docsCounter.set(0);
        } catch (Exception e) {
            logger.error("Unable to commit documents before deleting", e);
        }
        try {
            server.deleteByQuery(query);
            server.commit();
        } catch (Exception e) {
            logger.error("Delete query failed", e);
        }
    }

    @Override
    public void shutdown() {
        container.shutdown();
    }

    public String getSolrPath() {
        return solrPath;
    }
}