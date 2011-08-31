package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;

import java.util.HashMap;
import java.util.Map;

public class AlertsFixture extends DoFixture {
    private String host;
    private int port;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void subscribeToFilter(String email, String filter) throws Exception {
        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "alertsAction");
        params.put("subaction", "subscribe");
        params.put("filter", filter);
        params.put("email", email);
        lr.get(params);
    }

    public boolean hasAlertsWithFilter(String filter) throws Exception {
        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "alertsAction");
        params.put("subaction", "getAlerts");
        params.put("filter", filter);
        String response = lr.get(params);

        return !response.equals("[]");
    }

    public boolean notHaveAlerts(String filter) throws Exception {
        return !hasAlertsWithFilter(filter);
    }

    public void markAlertsWithFilters(String[] filters) throws Exception {
        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "alertsAction");
        params.put("subaction", "removeFilter");
        for (String filter : filters) {
            params.put("filter", filter);
            lr.get(params);
            params.remove(filter);
        }
    }

    public boolean hasNoMoreTestAlertsWithFilters(String[] filters) throws Exception {
        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        boolean res = true;

        params.put("action", "alertsAction");
        params.put("subaction", "getAlerts");
        for (String filter : filters) {
            params.put("filter", filter);
            String response = lr.get(params);
            params.remove(filter);
            res = res && response.equals("[]");
        }

        return res;
//        params.put("action", "alertsAction");
//        params.put("subaction", "getFilters");
//        String response = lr.get(params);
//        return response.equals("[]");
    }
}
