package com.griddynamics.logtool;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.*;
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
    public synchronized void addMessage(String[] path, String timestamp, String message) {
        if (needToWipe()) {
            wipe();
        }

        String[] clearPath = removeNullAndEmptyPathSegments(path);

        String logPath = buildPath(clearPath);
        File dir = new File(logPath);
        dir.mkdirs();
        String fileName = addToPath(logPath, constructFileName(timestamp));
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName, true);
            fileWriter.append(message);

        } catch (IOException ex) {
            logger.error("Tried to append message to: " + fileName, ex);
            return;
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException ex) {
                logger.error("Tried to close file: " + fileName, ex);
                return;
            }
        }

        addToFileSystem(fileSystem, clearPath);
        File log = new File(fileName);
        curFolderSize += log.length();
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public synchronized List<String> getLog(String[] path, String logName) {
        String[] clearPath = removeNullAndEmptyPathSegments(path);
        String logPath = addToPath(buildPath(clearPath), logName);
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
            logger.error("Tried to read log file: " + logPath, ex);
            return new ArrayList<String>();
        }
    }

    @Override
    public synchronized void deleteLog(String[] path, String ... names) {
        String[] clearPath = removeNullAndEmptyPathSegments(path);
        String logPath = buildPath(clearPath);
        if (names.length == 0) {
            deleteDirectory(clearPath);
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
                deleteDirectory(clearPath);
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
            String[] clearPath = removeNullAndEmptyPathSegments(path);
            Tree node = new Tree();
            File folder = new File(buildPath(clearPath));
            String[] logs = folder.list();
            for (String log : logs) {
                node.getChildren().put(log, null);
            }
            return node;
        } else {
            String[] clearPath = removeNullAndEmptyPathSegments(path);
            Tree node = fileSystem;
            for (int i = 0; i < clearPath.length; i++) {
                if (node.getChildren().containsKey(clearPath[i])) {
                    node = node.getChildren().get(clearPath[i]);
                } else {
                    logger.error("Couldn't found path: " + buildPath(clearPath));
                    return new Tree();
                }
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
            if (!deleteDirectory(f)) {
                logger.error("Couldn't delete directory: " + f.getAbsolutePath());
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
                res += measureSize(addToPath(path,file));
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
            if (!deleteDirectory(log)) {
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

    private boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                if (!deleteDirectory(new File(addToPath(dir.getAbsolutePath(), child)))) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private String buildPath(String ... path) {
        StringBuffer result = new StringBuffer(logFolder);
        for (int i = 0; i < path.length; i++) {
            result.append(File.separator).append(path[i]);
        }
        return result.toString();
    }

    private String addToPath(String path, String subPath) {
        return new StringBuffer(path).append(File.separator).append(subPath).toString();
    }

    private String constructFileName(String timestamp) {
        try {
            DateTime dateTime = new DateTime(timestamp);
            return new StringBuffer().append(dateTime.getYear()).append("-").append(dateTime.dayOfMonth().getAsShortText()).append("-").append(dateTime.monthOfYear().getAsShortText()).append(".log").toString();
        } catch (Exception ex) {
            logger.error("Couldn't parse date format: " + timestamp, ex);
            return "default.log";
        }
    }

    private String[] removeNullAndEmptyPathSegments(String[] path) {
        List<String> pathList = new ArrayList<String>();
        for(String pathSegment : path) {
            if (pathSegment != null && pathSegment.replace(" ", "").length() > 0) {
                pathList.add(pathSegment);
            }
        }
        return pathList.toArray(new String[pathList.size()]);
    }
}
