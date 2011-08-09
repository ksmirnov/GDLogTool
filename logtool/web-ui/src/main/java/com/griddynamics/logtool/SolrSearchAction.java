package com.griddynamics.logtool;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.List;
import java.util.Map;

public class SolrSearchAction extends Action {
    public void perform(HttpServletRequest req, HttpServletResponse resp) {
        try {
            doSearch(req.getParameter("query"), resp.getOutputStream());
        } catch (IOException ex) {

        }
    }

    public void doSearch(String request, ServletOutputStream sos) {
        try {
            sos.print(getJsonFromListMap(searchServer.search(request)));
        } catch (IOException ex) {

        }
    }

    private String getJsonFromListMap(List<Map<String, String>> list) {
        StringBuilder stringBuilder = new StringBuilder("occurrences = [");
        if (!list.isEmpty()) {
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

    private String getJsonFromMap(Map<String, String> map) {
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
