package com.griddynamics.logtool.fixtures;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class PathConstructor {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormat.forPattern("yyyy-dd-MMM").withLocale(Locale.ENGLISH);

    private static String fs = System.getProperty("file.separator");

    public static String reversePath(String path) {
        String[] pathSegments = path.split(fs);
        StringBuilder res = new StringBuilder();
        for (int i = pathSegments.length - 1; i >= 0; i--) {
            res.append(pathSegments[i]).append(fs);
        }
        return res.toString();
    }

    public static List<String> getPathSegments(String pathString) {
        StringTokenizer stTok = new StringTokenizer(pathString, fs);
        List<String> pathList = new LinkedList<String>();
        while (stTok.hasMoreElements()) {
            pathList.add(stTok.nextToken());
        }
        return pathList;
    }

    public static String getPath(List<String> segments) {
        StringBuilder out = new StringBuilder();
        for(String s : segments) {
            out.append(s).append(fs);
        }
        out.append(FORMATTER.print(System.currentTimeMillis())).append(".log");
        return out.toString();
    }

    public static String getFilename() {
        return FORMATTER.print(System.currentTimeMillis()) + ".log";
    }

    public static String createPath(String application, String host, String instance) {
        return getFilename() + "/" + instance + "/" + host + "/" + application;
    }
}
