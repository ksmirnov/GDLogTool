package com.griddynamics.logtool;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolrSearchAction extends Action {
    public void perform(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String subAction = req.getParameter("subaction");
            if (subAction.equals("solrsearch")) {
                doSolrSearch(req.getParameter("query"), resp.getOutputStream());
            } else {
                doGrepOverSolr(req.getParameter("query"), req.getParameter("request"), Integer.parseInt(req.getParameter("pageSize")), resp.getOutputStream());
            }
        } catch (IOException ex) {

        }
    }

    public void doGrepOverSolr(String query, String request, int pageSize, ServletOutputStream sos) {
            List<Map<String, String>> lol = new ArrayList<Map<String, String>>();
            Map<String, String> sad = new HashMap<String, String>();
            sad.put("path", "/home/vpolyakov/workspace/GDLogTool/logs/testApp/localhost/testInstance/2011-15-Jul.log");
            sad.put("startIndex", "10");
            sad.put("length", "1100");
            sad.put("application", "testApp");
            sad.put("host", "localhost");
            sad.put("instance", "testInstance");
            sad.put("date", "2011-15-Jul");
            lol.add(sad);
            sad = new HashMap<String, String>();
            sad.put("path", "/home/vpolyakov/workspace/GDLogTool/logs/testApp/localhost/testInstance1/2011-15-Jul.log");
            sad.put("startIndex", "10");
            sad.put("length", "480");
            sad.put("application", "testApp");
            sad.put("host", "localhost");
            sad.put("instance", "testInstance1");
            sad.put("date", "2011-15-Jul");
            lol.add(sad);

        try {
            sos.print(storage.doGrepOverSolrSearch(lol, request, pageSize).toString());
            //sos.print(storage.doGrepOverSolrSearch(searchServer.search(query), request, pageSize).toString());
        } catch (IOException ex) {

        }
    }

    public void doSolrSearch(String query, ServletOutputStream sos) {
            List<Map<String, String>> lol = new ArrayList<Map<String, String>>();
            Map<String, String> sad = new HashMap<String, String>();
            sad.put("path", "/home/vpolyakov/workspace/GDLogTool/logs/testApp/localhost/testInstance/2011-15-Jul.log");
            sad.put("startIndex", "10");
            sad.put("length", "1100");
            sad.put("application", "testApp");
            sad.put("host", "localhost");
            sad.put("instance", "testInstance");
            sad.put("date", "2011-15-Jul");
            lol.add(sad);
            sad = new HashMap<String, String>();
            sad.put("path", "/home/vpolyakov/workspace/GDLogTool/logs/testApp/localhost/testInstance1/2011-15-Jul.log");
            sad.put("startIndex", "10");
            sad.put("length", "480");
            sad.put("application", "testApp");
            sad.put("host", "localhost");
            sad.put("instance", "testInstance1");
            sad.put("date", "2011-15-Jul");
            lol.add(sad);

        try {
            sos.print(getJsonFromListMap(lol));
            //sos.print(getJsonFromListMap(searchServer.search(query)));
        } catch (IOException ex) {

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
