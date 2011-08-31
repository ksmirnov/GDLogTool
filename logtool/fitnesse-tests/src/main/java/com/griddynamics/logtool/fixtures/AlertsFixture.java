package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;

import java.util.HashMap;
import java.util.Map;

public class AlertsFixture extends DoFixture {
    
    public void atHostWithPortSubscribeToFilter(String host, int port, String email, String filter) throws Exception {
        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "alertsAction");
        params.put("subaction", "subscribe");
        params.put("filter", filter);
        params.put("email", email);
        lr.get(params);
    }

    public boolean hostWithPortHasAlertsWithFilter(String host, int port, String filter) throws Exception {
        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "alertsAction");
        params.put("subaction", "getAlerts");
        params.put("filter", filter);
        String response = lr.get(params);
        return !response.equals("[]");
    }

    public boolean notHaveAlerts(String host, int port, String filter) throws Exception {
        return !hostWithPortHasAlertsWithFilter(host, port, filter);
    }

    public void atHostWithPortMarkAlertsWithFilters(String host, int port, String[] filters) throws Exception {
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

    public boolean hostWithPortHasNoMoreTestAlerts(String host, int port) throws Exception {
        LogtoolRequester lr = new LogtoolRequester(host, port);
        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "alertsAction");
        params.put("subaction", "getFilters");
        String response = lr.get(params);
        return response.equals("[]");
    }
}
