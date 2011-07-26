package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class DeleteDirectoryAction implements Action {
    private Storage storage;

    public void setStorage(Storage storage){
        this.storage = storage;
    }

    public String perform(HttpServletRequest req, HttpServletResponse resp) {
        deleteDirectory(req.getParameter("path"));
        return "";
    }

    public String[] getPath(String pathString) {
        StringTokenizer stTok = new StringTokenizer(pathString, "/");
        List<String> pathList = new LinkedList<String>();
        while (stTok.hasMoreElements()) {
            pathList.add(0, stTok.nextToken());
        }
        return pathList.toArray(new String[0]);
    }

    public void deleteDirectory(String pathString) {
        storage.deleteDirectory(getPath(pathString));
    }
}
