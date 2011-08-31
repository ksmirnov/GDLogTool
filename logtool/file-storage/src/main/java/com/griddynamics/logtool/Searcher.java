package com.griddynamics.logtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
    private int startPagePos = -2;
    private int endPagePos = -2;
    private int startPage = -2;
    private int endPage = -2;

    private Map<String, FileChannel> openFiles = null;

    public Searcher(String request, int pageSize, Map<String, FileChannel> openFiles) {
        this.request = request;
        this.pageSize = pageSize;
        this.actualPageSize = pageSize + request.length() - 1;
        this.openFiles = openFiles;
    }

    public Map<String, Map<Integer, List<Integer>>> doSolrSearch(List<Map<String, String>> solrSearchResult) throws IOException {
        if(solrSearchResult != null) {
            for (Map<String, String> app : solrSearchResult) {
                String path = app.get("path");

                if (!openFiles.containsKey(path)) {
                    RandomAccessFile log = new RandomAccessFile(path, "r");
                    openFiles.put(path, log.getChannel());
                }
                FileChannel fc = openFiles.get(path);

                long startPos = Long.parseLong(app.get("startIndex"));
                long length = Long.parseLong(app.get("length"));
                startPagePos = (int) (startPos % pageSize);
                endPagePos = (int) (startPos + length) % pageSize;
                startPage = (int) (startPos / pageSize);
                endPage = (int) ((startPos + length) / pageSize);

                ByteBuffer buf = ByteBuffer.allocate(actualPageSize);

                StringBuffer sb = new StringBuffer(path + "||<>||");
                sb.append(app.get("application")).append(" / ");
                sb.append(app.get("host")).append(" / ");
                sb.append(app.get("instance")).append(" / ");
                sb.append(app.get("timestamp"));
                sb.append(" (").append(startPos).append(", ").append(length).append(")");

                for (int i = startPage; i <= endPage; i++) {
                    long curPos = i * pageSize;
                    String chunk = null;
                    int bytesRead = fc.read(buf, curPos);
                    if (bytesRead == actualPageSize) {
                        chunk = new String(buf.array());
                    } else {
                        ByteBuffer buff = ByteBuffer.allocate(bytesRead);
                        fc.read(buff, curPos);
                        chunk = new String(buff.array());
                    }

                    buf.clear();
                    inStringSearch(chunk, i + 1, actualPageSize, sb.toString());
                }
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
            if (!openFiles.containsKey(dir.getAbsolutePath())) {
                RandomAccessFile log = new RandomAccessFile(dir.getAbsolutePath(), "r");
                openFiles.put(dir.getAbsolutePath(), log.getChannel());
            }
            FileChannel fc = openFiles.get(dir.getAbsolutePath());

            ByteBuffer buf = ByteBuffer.allocate(actualPageSize);
            long parts = fc.size() / pageSize;
            for (int i = 1; i < parts + 2; i++) {
                String chunk = null;
                int bytesRead = fc.read(buf, (i - 1) * pageSize);
                if (bytesRead == actualPageSize) {
                    chunk = new String(buf.array());
                } else {
                    ByteBuffer buff = ByteBuffer.allocate(bytesRead);
                    fc.read(buff, (i - 1) * pageSize);
                    chunk = new String(buff.array());
                }

                buf.clear();
                inStringSearch(chunk, i, actualPageSize, dir.getAbsolutePath());
            }
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
                if ((chunkNumber - 1 == startPage && index < startPagePos) || (chunkNumber - 1 == endPage && index >= endPagePos)) {
                    pos = index + 1;
                    continue;
                }
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
