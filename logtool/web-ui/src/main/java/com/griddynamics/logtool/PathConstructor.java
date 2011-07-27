package com.griddynamics.logtool;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class PathConstructor {
    public static List<String> getPath(String pathString) {
        StringTokenizer stTok = new StringTokenizer(pathString, "/");
        List<String> pathList = new LinkedList<String>();
        while (stTok.hasMoreElements()) {
            pathList.add(0,stTok.nextToken());
        }
        return pathList;
    }

}
