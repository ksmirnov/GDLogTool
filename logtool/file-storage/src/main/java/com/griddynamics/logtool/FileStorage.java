package com.griddynamics.logtool;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileStorage implements Storage {
    private String logFolder = "";
    private long maxFolderLength = (long) 1 << 40;
    private long curFolderLength = 0;
    private Map<String, Object> fileSystem = new HashMap<String, Object>();
    private long lastUpdateTime = System.currentTimeMillis();

    public void setLogFolder(String logFolder) {
        this.logFolder = logFolder;
    }

    public void setMaxLogFolderLength(long length) {
        this.maxFolderLength = length;
    }

    public synchronized long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public FileStorage() throws IOException {
        File settings = new File("settings.cfg");
        if (settings.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader("settings.cfg"));
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("Log Folder = ")) {
                    logFolder = line.substring(13);
                }
                if (line.contains("Max Folder Length = ")) {
                    maxFolderLength = Integer.parseInt(line.substring(20));
                }
                line = reader.readLine();
            }
            reader.close();
        }

        createFileSystem(logFolder, fileSystem);
    }

    public synchronized void saveLog(String application, String host, String instance,
            Date date, String message) throws IOException {
        if (needToWipe()) {
            wipe();
        }

        fileSystem.put(application, (new HashMap<String, Object>()).put(host,
                (new HashMap<String, Object>()).put(instance,
                (new HashMap<String, Object>()).put(constructFileName(date), null))));

        lastUpdateTime = System.currentTimeMillis();

        String path = logFolder + "/" + application + "/" + host + "/" + instance;
        File dir = new File(path);
        dir.mkdirs();
        String fileName = path  + "/" + constructFileName(date);
        FileWriter fileWriter = new FileWriter(fileName, true);
        fileWriter.append(message);
        fileWriter.close();

        curFolderLength += message.length();
    }

    public synchronized List<String> getLog(String application, String host,
            String instance, Date date) throws IOException {
        String logPath = logFolder + "/" + application + "/" + host +
                "/" + instance + "/" + constructFileName(date);
        BufferedReader reader = new BufferedReader(new FileReader(logPath));
        List<String> log = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            log.add(line);
        }
        return log;
    }

    public synchronized void deleteLog(String application, String host,
            String instance, Date date) throws IOException {
        lastUpdateTime = System.currentTimeMillis();

        String logApplication = logFolder + "/" + application;
        String logHost = !host.equals("-1") ? "/" + host : "";
        String logInstance = !instance.equals("-1") ? "/" + instance : "";
        String logName = date != null ? "/" + constructFileName(date) : "";
        String logPath = logApplication + logHost + logInstance + logName;
        File log = new File(logPath);
        log.delete();

        Map<String, Object> hosts, instances, dates;
        if (host.equals("-1")) {
            fileSystem.remove(application);
        } else {
            hosts = (HashMap<String, Object>)fileSystem.get(application);
            if (instance.equals("-1")) {
                hosts.remove(host);
            } else {
                instances = (HashMap<String, Object>)hosts.get(host);
                if (date == null) {
                    instances.remove(host);
                } else {
                    dates = (HashMap<String, Object>)instances.get(instance);
                    dates.remove(constructFileName(date));
                }
            }
        }
    }

    public synchronized Map<String, Object> getTree() {
        return fileSystem;
    }

    public synchronized String[] getSubTree(String application, String host, String instance) {
        File logs = new File(logFolder + "/" + application + "/" + host + "/"
                + instance + "/");
        return logs.list();
    }

    private String constructFileName(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM");
        return simpleDateFormat.format(date) + ".log";
    }

    private boolean needToWipe() {
        return curFolderLength < maxFolderLength;
    }

    private void wipe() {
        File root = new File(logFolder);
        File[] files = root.listFiles();
        for (File f : files) {
            f.delete();
        }
        curFolderLength = 0;
        fileSystem.clear();
    }

    private void createFileSystem(String path, Map<String, Object> fileSystem) {
        File dir = new File(path);
        String[] names = dir.list();
        if (dir.isDirectory()) {
            for (String name : names) {
                Map<String, Object> fs = new HashMap<String, Object>();
                fileSystem.put(name, fs);
                createFileSystem(path + "/" + name, fs);
            }
        }
    }
}
