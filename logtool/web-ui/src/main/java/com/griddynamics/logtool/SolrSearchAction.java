package com.griddynamics.logtool;

import org.json.simple.JSONObject;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SolrSearchAction extends Action {
    public void perform(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String subAction = req.getParameter("subaction");
            if (subAction.equalsIgnoreCase("solrsearch")) {
                doSolrSearch(req.getParameter("query"), Integer.parseInt(req.getParameter("start")),
                        Integer.parseInt(req.getParameter("amount")), req.getParameter("sortField"),
                        req.getParameter("order"), resp.getOutputStream());
            } else if (subAction.equalsIgnoreCase("getfacets")) {
                getFacets(req.getParameter("filter"), resp.getOutputStream());
            } else {
                doGrepOverSolr(req.getParameter("query"), req.getParameter("request"),
                        Integer.parseInt(req.getParameter("pageSize")), resp.getOutputStream());
            }
        } catch (IOException ex) {

        }
    }

    public void doGrepOverSolr(String query, String request, int pageSize, ServletOutputStream sos) {
        try {
            sos.print(storage.doGrepOverSolrSearch(searchServer.search(query, -1, 0, "", ""), request, pageSize).toString());
        } catch (IOException ex) {

        }
    }

    public void doSolrSearch(String query, int start, int amount, String sortField, String order, ServletOutputStream sos) {
        try {
            sos.print(getJsonFromListMap(searchServer.search(query, start, amount, sortField, order)));
        } catch (IOException ex) {

        }
    }

    public void getFacets(String filter, ServletOutputStream sos) {
        Set<Facet> facets = searchServer.getFacets(filter);
        if(!facets.isEmpty()) {
            try {
                sos.print(getJsonFromFacetsSet(facets));
            } catch (IOException ex) {

            }
        }
    }

    private String getJsonFromListMap(List<Map<String, String>> list) {
        StringBuilder stringBuilder = new StringBuilder("occurrences = [");
        if (list != null && !list.isEmpty()) {
            for (Map<String, String> map : list) {
                stringBuilder.append(getJsonFromMap(map)).append(", ");
            }
        }
        int index = stringBuilder.lastIndexOf(", ");
        if (index > -1) {
            stringBuilder.replace(index, index + 2, "");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private String getJsonFromFacetsSet(Set<Facet> facets) {
        StringBuilder out = new StringBuilder();
        out.append("[");
        for(Facet f : facets) {
            out.append("{text: '" + f.getName() + "', expanded: true, children: [");
            for(String s : f.getCount().keySet()) {
                if(f.getCount().get(s) > 0) {
                    out.append("{text: '" + s + " (" + f.getCount().get(s) + ")', leaf: true, checked: false},");
                }
            }
            out.setLength(out.length() - 1);
            out.append("]},");
        }
        out.setLength(out.length() - 1);
        out.append("]");
        return JSONObject.escape(out.toString());
    }

    private String getJsonFromMap(Map<String, String> map) {
        map.remove("content");
        StringBuilder stringBuilder = new StringBuilder("{");
        for (Map.Entry<String, String> pair : map.entrySet()) {
            stringBuilder.append("\"").append(pair.getKey()).append("\":\"").append(pair.getValue()).append("\", ");
        }
        int index = stringBuilder.lastIndexOf(", ");
        if (index > -1) {
            stringBuilder.replace(index, index + 2, "");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
