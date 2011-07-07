package com.griddynamics.logtool;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileStorage implements Storage {
    private static final Logger logger = LoggerFactory.getLogger(FileStorage.class);

    private String logFolder = "";
    private long maxFolderSize;
    private long curFolderSize = 0;
    private Tree fileSystem = new Tree();
    private long lastUpdateTime = System.currentTimeMillis();

    @Required
    public void setLogFolder(String logFolder) {
        this.logFolder = logFolder;
    }

    @Required
    public void setMaxFolderSize(long length) {
        this.maxFolderSize = length << 20;
    }

    @Override
    public synchronized long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    @Override
    public void addMessage(String[] path, String timestamp, String message) {
        if (needToWipe()) {
            wipe();
        }

        addToFileSystem(fileSystem, path);

        String logPath = buildPath(path);
        File dir = new File(logPath);
        dir.mkdirs();
        String fileName = addToPath(logPath, constructFileName(timestamp));
        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            fileWriter.append(message);
            fileWriter.close();
        } catch (IOException ex) {
            logger.error("IOException occurred: [{}]", ex);
            return;
        }

        File log = new File(fileName);
        curFolderSize += log.length();
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public synchronized List<String> getLog(String[] path, String logName) {
        String logPath = addToPath(buildPath(path), logName);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logPath));
            List<String> log = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null) {
                log.add(line);
                line = reader.readLine();
            }
            return log;
        } catch (IOException ex) {
            logger.error("IOException occurred: [{}]", ex);
            return new ArrayList<String>();
        }
    }

    @Override
    public synchronized void deleteLog(String[] path, String ... names) {
        String logPath = buildPath(path);
        if (names.length == 0) {
            deleteDirectory(path);
        } else {
            for (String name : names) {
                String logAbsolutePath = addToPath(logPath, name);
                File log = new File(logAbsolutePath);
                long logSize = log.length();
                curFolderSize -= logSize;
                if (!log.delete()) {
                    curFolderSize += logSize;
                    logger.error("Couldn't delete log file: " + logAbsolutePath);
                }
            }
            File dir = new File(logPath);
            if (dir.list().length == 0) {
                deleteDirectory(path);
            }
        }

        lastUpdateTime = System.currentTimeMillis();
    }

    public void createTreeFromDisk() {
        fileSystem = createTreeFromDisk(logFolder);
    }

    private Tree createTreeFromDisk(String path) {
        File file = new File(path);
        File[] dirs = file.listFiles();
        boolean hasOnlyFiles = true;
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i].isDirectory()) hasOnlyFiles = false;
        }
        if (hasOnlyFiles) {
            return null;
        } else {
            Tree node = new Tree();
            for (File dir : dirs) {
                if (dir.isDirectory()) {
                    node.getChildren().put(dir.getName(), createTreeFromDisk(addToPath(path, dir.getName())));
                }
            }
            return node;
        }
    }


    @Override
    public synchronized Tree getTree(int height, String ... path) {
        if (height == -1) {
            return fileSystem;
        } else if (height == 0) {
            Tree node = new Tree();
            File folder = new File(buildPath(path));
            String[] logs = folder.list();
            for (String log : logs) {
                node.getChildren().put(log, null);
            }
            return node;
        } else {
            Tree node = fileSystem;
            for (int i = 0; i < path.length; i++) {
                node = node.getChildren().get(path[i]);
            }
            return getTree(height, node);
        }
    }

    private Tree getTree(int height, Tree curNode) {
        if (curNode == null) return null;
        Tree node = new Tree(curNode);
        for (String key : curNode.getChildren().keySet()) {
            if (height > 0) {
                node.getChildren().put(key, getTree(height - 1, curNode.getChildren().get(key)));
            } else {
                node.getChildren().put(key, null);
            }
        }
        return node;
    }

    private void addToFileSystem(Tree curNode, String ... path) {
        if (path.length == 1) {
            curNode.getChildren().put(path[0], null);
            return;
        }
        if (curNode.getChildren().containsKey(path[0])) {
            addToFileSystem(curNode.getChildren().get(path[0]), getSubPath(path));
        } else {
            Tree node = new Tree();
            curNode.getChildren().put(path[0], node);
            addToFileSystem(node, getSubPath(path));
        }

    }

    private String[] getUpPath(String ... path) {
        return Arrays.copyOf(path, path.length - 1);
    }

    private String[] getSubPath(String ... path) {
        return Arrays.copyOfRange(path, 1, path.length);
    }

    private boolean needToWipe() {
        return curFolderSize > maxFolderSize;
    }

    private void wipe() {
        File root = new File(logFolder);
        File[] files = root.listFiles();
        for (File f : files) {
            if (!f.delete()) {
                logger.error("Couldn't delete log file: " + f.getAbsolutePath());
            }
        }
        curFolderSize = 0;
        fileSystem.getChildren().clear();
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

    private void deleteDirectory(String ... path) {
        if (path.length > 0) {
            String logPath = buildPath(path);
            long logDirSize = measureSize(logPath);

            File log = new File(logPath);
            if (!log.delete()) {
                logger.error("Couldn't delete directory: " + logPath);
            } else {
                curFolderSize -= logDirSize;
                Tree node = fileSystem;
                for (int i = 0; i < path.length - 1; i++) {
                    node = node.getChildren().get(path[i]);
                }
                node.getChildren().remove(path[path.length - 1]);
            }

            String[] upPath = getUpPath(path);
            logPath = buildPath(upPath);
            log = new File(logPath);
            if (log.list().length == 0) {
                deleteDirectory(upPath);
            }
        }
    }

    private String buildPath(String ... path) {
        StringBuilder result = new StringBuilder(logFolder);
        for (int i=0; i < path.length; i++) {
            result.append("/").append(path[i]);
        }
        return result.toString();
    }

    private String addToPath(String path, String subPath) {
        return new StringBuilder(path).append("/").append(subPath).toString();
    }

    private String constructFileName(String timestamp) {
        DateTime dateTime = new DateTime(timestamp);
        return new StringBuilder().append(dateTime.getYear()).append(" ").append(dateTime.getDayOfMonth()).append(" ").append(dateTime.monthOfYear().getAsShortText()).append(".log").toString();
    }
}
