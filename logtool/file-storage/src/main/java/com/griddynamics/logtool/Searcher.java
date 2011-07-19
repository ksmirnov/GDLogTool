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

    public Map<String, Map<Integer, List<Integer>>> doSearchNew(String path) throws IOException {
        File dir = new File(path);
        File[] logs = dir.listFiles();

        for (File log : logs) {
            if (log.isFile()) {
                RandomAccessFile rafLog = null;
                rafLog = new RandomAccessFile(log.getAbsolutePath(), "r");
                byte[] buf = new byte[actualPageSize];
                long parts = rafLog.length() / pageSize;
                for (int i = 0; i < parts + 1; i++) {
                    rafLog.seek(i * pageSize);
                    String chunk = null;
                    int bytesRead = rafLog.read(buf);
                    if (bytesRead == actualPageSize) {
                        chunk = new String(buf);
                    } else {
                        byte[] buff = new byte[bytesRead];
                        rafLog.seek(i * pageSize);
                        rafLog.read(buff);
                        chunk = new String(buff);
                    }

                    inStringSearch(chunk, i, pageSize, log.getAbsolutePath());
                }
                rafLog.close();
            } else {
                doSearchNew(log.getAbsolutePath());
            }
        }

        return results;
    }

    public Map<String, Map<Integer, List<Integer>>> doSearch(String path) throws IOException {
        File dir = new File(path);
        File[] logs = dir.listFiles();

        for (File log : logs) {
            if (log.isFile()) {
                BufferedReader brLog = null;
                brLog = new BufferedReader(new FileReader(log));
                int lineNumber = 1;
                String line = brLog.readLine();
                while (line != null) {
                    inStringSearch(line, lineNumber, line.length(), log.getAbsolutePath());
                    lineNumber++;
                    line = brLog.readLine();
                }
                brLog.close();
            } else {
                doSearch(log.getAbsolutePath());
            }
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
