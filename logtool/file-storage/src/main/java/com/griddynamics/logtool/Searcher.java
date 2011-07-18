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
    private static final int PAGE_SIZE = 4 << 10;

    private String folderPath;
    private String request;
    private Map<String, Map<Integer, List<Integer>>> results = new HashMap<String, Map<Integer, List<Integer>>>();
    private int actualPageSize;

    public Searcher(String folderPath, String request) {
        this.folderPath = folderPath;
        this.request = request;
        this.actualPageSize = PAGE_SIZE + request.length() - 1;
    }

    public Map<String, Map<Integer, List<Integer>>> doSearch() {
        return search(folderPath);
    }

    private Map<String, Map<Integer, List<Integer>>> doSearch(String path) {
        File dir = new File(path);
        File[] logs = dir.listFiles();

        for (File log : logs) {
            if (log.isFile()) {
                RandomAccessFile rafLog = null;
                try {
                    rafLog = new RandomAccessFile(log.getAbsolutePath(), "r");
                    byte[] buf = new byte[actualPageSize];
                    long parts = rafLog.length() / PAGE_SIZE;
                    for (int i = 0; i < parts + 1; i++) {
                        rafLog.seek(i * PAGE_SIZE);
                        String chunk = null;
                        int bytesRead = rafLog.read(buf);
                        if (bytesRead == actualPageSize) {
                            chunk = new String(buf);
                        } else {
                            byte[] buff = new byte[bytesRead];
                            rafLog.seek(i * PAGE_SIZE);
                            rafLog.read(buff);
                            chunk = new String(buff);
                        }

                        int pos = -1;
                        while (pos < PAGE_SIZE) {
                            int index = chunk.indexOf(request, pos + 1);
                            if (index >= 0) {
                                if (!results.containsKey(log.getAbsolutePath())) {
                                    results.put(log.getAbsolutePath(), new HashMap<Integer, List<Integer>>());
                                }
                                if (!results.get(log.getAbsolutePath()).containsKey(i)) {
                                    results.get(log.getAbsolutePath()).put(i, new ArrayList<Integer>());
                                }
                                results.get(log.getAbsolutePath()).get(i).add(index);
                                pos = index + 1;
                            } else {
                                pos = PAGE_SIZE;
                            }
                        }
                    }
                    rafLog.close();
                } catch (FileNotFoundException ex) {
                    logger.error("Tried to read log file: " + log.getAbsolutePath(), ex);
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            } else {
                doSearch(log.getAbsolutePath());
            }
        }

        return results;
    }

    private Map<String, Map<Integer, List<Integer>>> search(String path) {
        File dir = new File(path);
        File[] logs = dir.listFiles();

        for (File log : logs) {
            if (log.isFile()) {
                BufferedReader brLog = null;
                try {
                    brLog = new BufferedReader(new FileReader(log));
                    int lineNumber = 1;
                    String line = brLog.readLine();
                    while (line != null) {
                        int pos = -1;
                        while (pos < line.length()) {
                            int index = line.indexOf(request, pos + 1);
                            if (index >= 0) {
                                if (!results.containsKey(log.getAbsolutePath())) {
                                    results.put(log.getAbsolutePath(), new HashMap<Integer, List<Integer>>());
                                }
                                if (!results.get(log.getAbsolutePath()).containsKey(lineNumber)) {
                                    results.get(log.getAbsolutePath()).put(lineNumber, new ArrayList<Integer>());
                                }
                                results.get(log.getAbsolutePath()).get(lineNumber).add(index);
                                pos = index + 1;
                            } else {
                                pos = line.length();
                            }
                        }
                        lineNumber++;
                        line = brLog.readLine();
                    }
                    brLog.close();
                } catch (FileNotFoundException ex) {
                    logger.error("Tried to read log file: " + log.getAbsolutePath(), ex);
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            } else {
                search(log.getAbsolutePath());
            }
        }

        return results;
    }
}
