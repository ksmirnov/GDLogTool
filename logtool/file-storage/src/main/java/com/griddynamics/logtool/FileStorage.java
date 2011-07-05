package com.griddynamics.logtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileStorage implements Storage {
    private String logFolder = "";
    private long maxFolderSize = (long) 1 << 40;
    private long curFolderSize = 0;
    private Map<String, Object> fileSystem = new HashMap<String, Object>();
    private long lastUpdateTime = System.currentTimeMillis();

    public void setLogFolder(String logFolder) {
        this.logFolder = logFolder;
    }

    public void setMaxFolderSize(long length) {
        this.maxFolderSize = length;
    }

    public synchronized long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    private final String FOLDER_SETTING = "Log Folder = ";
    private final String FOLDER_SIZE_SETTING = "Max Folder Size = ";
    private final String SETTINGS_FILE_NAME = "settings.cfg";
    private final int MAX_DEPTH = 2;

    private static final Logger logger = LoggerFactory.getLogger(FileStorage.class);


    public synchronized void saveLog(String application, String host, String instance,
            Date date, String message) {
        if (needToWipe()) {
            wipe();
        }

        Map<String, Object> hosts;
        List<String> instances;
        if (fileSystem.containsKey(application)) {
            hosts = (HashMap<String, Object>) fileSystem.get(application);
        } else {
            hosts = new HashMap<String, Object>();
            fileSystem.put(application, hosts);
        }
        if (hosts.containsKey(host)) {
            instances = (ArrayList<String>) hosts.get(host);
        } else {
            instances = new ArrayList<String>();
            hosts.put(host, instances);
        }
        if (!instances.contains(instance)) {
            instances.add(instance);
        }

        String path = logFolder + "/" + application + "/" + host + "/" + instance;
        File dir = new File(path);
        dir.mkdirs();
        String fileName = path  + "/" + constructFileName(date);
        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            fileWriter.append(message);
            fileWriter.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            return;
        }

        File log = new File(fileName);
        curFolderSize += log.length();
        lastUpdateTime = System.currentTimeMillis();
    }

    public synchronized List<String> getLog(String application, String host,
            String instance, Date date) {
        String logPath = logFolder + "/" + application + "/" + host +
                "/" + instance + "/" + constructFileName(date);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logPath));
            List<String> log = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null) {
                log.add(line);
            }
            return log;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            return null;
        }
    }

    public synchronized void deleteLog(String application, String host,
            String instance, Date date) {
        lastUpdateTime = System.currentTimeMillis();
        deleteLogFile(application, host, instance, date);

        Map<String, Object> hosts = (HashMap<String, Object>) fileSystem.get(application);;
        List<String> instances = (ArrayList<String>) hosts.get(host);

        String logDir = logFolder + "/" + application + "/" + host + "/" + instance;
        File dir = new File(logDir);
        if (dir.list().length == 0) {
            instances.remove(instance);
            if (instances.isEmpty()) {
                hosts.remove(host);
                if (hosts.isEmpty()) {
                    fileSystem.remove(application);
                }
            }
        }
    }

    public synchronized void deleteLog(String application, String host, String instance) {
        lastUpdateTime = System.currentTimeMillis();
        deleteLogFile(application, host, instance, null);

        Map<String, Object> hosts = (HashMap<String, Object>) fileSystem.get(application);
        List<String> instances = (ArrayList<String>) hosts.get(host);

        instances.remove(instance);
        if (instances.isEmpty()) {
            hosts.remove(host);
            if (hosts.isEmpty()) {
                fileSystem.remove(application);
            }
        }
    }

    public synchronized void deleteLog(String application, String host) {
        lastUpdateTime = System.currentTimeMillis();
        deleteLogFile(application, host, "", null);

        Map<String, Object> hosts = (HashMap<String, Object>) fileSystem.get(application);

        hosts.remove(host);
        if (hosts.isEmpty()) {
            fileSystem.remove(application);
        }
    }

    public synchronized void deleteLog(String application) {
        lastUpdateTime = System.currentTimeMillis();
        deleteLogFile(application, "", "", null);

        fileSystem.remove(application);
    }

    public synchronized Map<String, Object> getTree() {
        return fileSystem;
    }

    public synchronized String[] getSubTree(String application, String host, String instance) {
        File logs = new File(logFolder + "/" + application + "/" + host + "/"
                + instance + "/");
        return logs.list();
    }

    public void initialize() {
        File settings = new File(SETTINGS_FILE_NAME);
        if (settings.exists() && settings.isFile()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE_NAME));
                String line = reader.readLine();
                while (line != null) {
                    if (line.contains(FOLDER_SETTING)) {
                        logFolder = line.substring(FOLDER_SETTING.length());
                    }
                    if (line.contains(FOLDER_SIZE_SETTING)) {
                        maxFolderSize = (long) Integer.parseInt(line.substring(FOLDER_SIZE_SETTING.length())) << 40;
                    }
                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException ex) {
                logger.error(ex.getMessage());
                return;
            }
        }

        createFileSystem(logFolder, fileSystem, 1);
        curFolderSize = measureSize(logFolder);
    }

    private String constructFileName(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM");
        return simpleDateFormat.format(date) + ".log";
    }

    private boolean needToWipe() {
        return curFolderSize < maxFolderSize;
    }

    private void wipe() {
        File root = new File(logFolder);
        File[] files = root.listFiles();
        for (File f : files) {
            if (!f.delete()) {
                logger.warn("Couldn't delete log file: " + f.getAbsolutePath());
            }
        }
        curFolderSize = 0;
        fileSystem.clear();
    }

    private void createFileSystem(String path, Map<String, Object> fileSystem, int level) {
        File dir = new File(path);
        String[] names = dir.list();
        if (level == MAX_DEPTH) {
            for (String name : names) {
                File inst = new File(path + "/" + name);
                fileSystem.put(name, Arrays.asList(inst.list()));
            }
        } else {
            for (String name : names) {
                Map<String, Object> fs = new HashMap<String, Object>();
                fileSystem.put(name, fs);
                createFileSystem(path + "/" + name, fs, level + 1);
            }
        }
    }

    private long measureSize(String path) {
        File dir = new File(path);
        if (dir.isDirectory()) {
            long res = 0;
            String[] files = dir.list();
            for (String file : files) {
                res += measureSize(path + "/" + file);
            }
            return res;
        } else {
            return dir.length();
        }
    }

    private void deleteLogFile(String application, String host, String instance, Date date) {
        String logApplication = logFolder + "/" + application;
        String logHost = !host.equals("") ? "/" + host : "";
        String logInstance = !instance.equals("") ? "/" + instance : "";
        String logName = date != null ? "/" + constructFileName(date) : "";
        String logPath = logApplication + logHost + logInstance + logName;
        File log = new File(logPath);
        if (!log.delete()) {
            logger.warn("Couldn't delete log file: " + logPath);
        }

        curFolderSize -= measureSize(logPath);
    }
}
