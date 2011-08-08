package com.griddynamics.logtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Searcher {
    private static final Logger logger = LoggerFactory.getLogger(Searcher.class);

    private String request;
    private Map<String, Map<Integer, List<Integer>>> results = new HashMap<String, Map<Integer, List<Integer>>>();
    private int pageSize;
    private int actualPageSize;

    public Searcher(String request, int pageSize) {
        this.request = request;
        this.pageSize = pageSize;
        this.actualPageSize = pageSize + request.length() - 1;
    }

    public Map<String, Map<Integer, List<Integer>>> doSolrSearch(List<Map<String, String>> solrSearchResult) throws IOException {
        for (Map<String, String> app : solrSearchResult) {
            String path = app.get("path");
            RandomAccessFile rafLog = new RandomAccessFile(path, "r");

            long startPos = Long.parseLong(app.get("startIndex"));
            long length = Long.parseLong(app.get("length"));
            long parts = length / pageSize;
            byte[] buf = new byte[actualPageSize];

            StringBuffer sb = new StringBuffer(path);
            sb = sb.append(" ").append(startPos).append(" ").append(length);

            for (int i = 1; i < parts + 1; i++) {
                long curPos = startPos + (i - 1) * pageSize;
                rafLog.seek(curPos);
                String chunk = null;
                int bytesRead = rafLog.read(buf);
                if (bytesRead == actualPageSize) {
                    chunk = new String(buf);
                } else {
                    byte[] buff = new byte[bytesRead];
                    rafLog.seek(curPos);
                    rafLog.read(buff);
                    chunk = new String(buff);
                }
                inStringSearch(chunk, i, actualPageSize, sb.toString());
            }

            long remainingBytes = length - parts * pageSize;
            if (remainingBytes > 0) {
                long curPos = startPos + parts * pageSize;
                rafLog.seek(curPos);
                String chunk = null;
                byte[] buff = new byte[(int) remainingBytes];
                rafLog.seek(curPos);
                rafLog.read(buff);
                chunk = new String(buff);
                inStringSearch(chunk, (int) parts + 1, actualPageSize, sb.toString());
            }
        }

        return results;
    }

    public Map<String, Map<Integer, List<Integer>>> doSearchNew(String path) throws IOException {
        File dir = new File(path);
        if (dir.isDirectory()) {
            File[] subdirs = dir.listFiles();
            for (File subdir : subdirs) {
                doSearchNew(subdir.getAbsolutePath());
            }
        } else {
            RandomAccessFile rafLog = null;
            rafLog = new RandomAccessFile(dir.getAbsolutePath(), "r");
            byte[] buf = new byte[actualPageSize];
            long parts = rafLog.length() / pageSize;
            for (int i = 1; i < parts + 2; i++) {
                rafLog.seek((i - 1) * pageSize);
                String chunk = null;
                int bytesRead = rafLog.read(buf);
                if (bytesRead == actualPageSize) {
                    chunk = new String(buf);
                } else {
                    byte[] buff = new byte[bytesRead];
                    rafLog.seek((i - 1) * pageSize);
                    rafLog.read(buff);
                    chunk = new String(buff);
                }
                inStringSearch(chunk, i, actualPageSize, dir.getAbsolutePath());
            }
            rafLog.close();
        }

        return results;
    }

    public Map<String, Map<Integer, List<Integer>>> doSearch(String path) throws IOException {
        File dir = new File(path);
        if (dir.isDirectory()) {
            File[] subdirs = dir.listFiles();
            for (File subdir : subdirs) {
                doSearch(subdir.getAbsolutePath());
            }
        } else {
            BufferedReader brLog = null;
            brLog = new BufferedReader(new FileReader(dir));
            int lineNumber = 1;
            String line = brLog.readLine();
            while (line != null) {
                inStringSearch(line, lineNumber, line.length(), dir.getAbsolutePath());
                lineNumber++;
                line = brLog.readLine();
            }
            brLog.close();
        }

        return results;
    }

    private void inStringSearch(String str, int chunkNumber, int maxLen, String absolutePath) {
        int pos = -1;
        while (pos < str.length()) {
            int index = str.indexOf(request, pos + 1);
            if (index >= 0) {
                if (!results.containsKey(absolutePath)) {
                    results.put(absolutePath, new HashMap<Integer, List<Integer>>());
                }
                if (!results.get(absolutePath).containsKey(chunkNumber)) {
                    results.get(absolutePath).put(chunkNumber, new ArrayList<Integer>());
                }
                results.get(absolutePath).get(chunkNumber).add(index);
                pos = index + 1;
            } else {
                pos = maxLen;
            }
        }
    }
}
