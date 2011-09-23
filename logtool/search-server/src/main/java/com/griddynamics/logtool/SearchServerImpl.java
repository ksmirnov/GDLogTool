package com.griddynamics.logtool;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
    private final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

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
    public List<Map<String, String>> search(String query, int start, int amount, String sortField, String order) {
        SolrQuery solrQuery = new SolrQuery(query);
        if (start != -1) {
            solrQuery.setRows(amount);
            solrQuery.setStart(start);
            solrQuery.setSortField(sortField, SolrQuery.ORDER.valueOf(order));
        } else {
            solrQuery.setRows(Integer.MAX_VALUE);
        }

        QueryResponse resp = search0(solrQuery);
        List out = new ArrayList();
        if(resp != null) {
            SolrDocumentList docs = resp.getResults();
            if(docs != null) {
                for(SolrDocument doc : docs) {
                    Map<String, String> entry = new HashMap<String, String>();
                    for(String name : doc.getFieldNames()) {
                        entry.put(name, doc.getFieldValue(name).toString());
                    }
                    out.add(entry);
                }
            }
        }
        return out;
    }

    protected QueryResponse search0(SolrQuery query) {
        try {
            server.commit();
            docsCounter.set(0);
        } catch (Exception e) {
            logger.error("Unable to commit documents before perform search query", e);
        }
        QueryResponse out;
        try {
            out = server.query(query);
            return out;
        } catch (Exception e) {
            logger.error("Search query failed", e);
            return null;
        }

    }

    @Override
    public Set<Facet> getFacets(String query) {
        SolrQuery solrQuery = new SolrQuery("*:*");
        solrQuery.setFacet(true);
        if(query != null && !query.isEmpty()) {
            solrQuery.addFilterQuery(query);
        }
        DateTime dt = new DateTime(System.currentTimeMillis());
        String currentDt = timeFormatter.print(dt);
        String lastHour = timeFormatter.print(dt.plusHours(-1));
        String lastDay = timeFormatter.print(dt.plusDays(-1));
        String lastWeek = timeFormatter.print(dt.plusWeeks(-1));
        solrQuery.addFacetQuery("timestamp:[" + lastHour + " TO " + currentDt + "]");
        solrQuery.addFacetQuery("timestamp:[" + lastDay + " TO " + currentDt + "]");
        solrQuery.addFacetQuery("timestamp:[" + lastWeek + " TO " + currentDt + "]");
        solrQuery.addFacetField("host", "application", "instance", "level");
        solrQuery.setRows(0);
        QueryResponse resp = search0(solrQuery);
        if(resp != null) {
            Set<Facet> out = new HashSet<Facet>();
            for(FacetField ff : resp.getFacetFields()) {
                Facet current = new Facet(ff.getName());
                if(ff.getValues() != null) {
                    for(FacetField.Count c : ff.getValues()) {
                        current.addCount(c.getName(), c.getCount());
                    }
                    out.add(current);
                }
            }
            Facet dates = new Facet("timestamp");
            Map<String, Integer> facetQuery = resp.getFacetQuery();
            Iterator it = facetQuery.keySet().iterator();
            dates.addCount("last hour", (long) facetQuery.get(it.next()));
            dates.addCount("last day", (long) facetQuery.get(it.next()));
            dates.addCount("last week", (long) facetQuery.get(it.next()));
            out.add(dates);
            return out;
        } else return null;
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