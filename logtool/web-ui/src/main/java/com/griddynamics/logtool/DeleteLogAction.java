package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class DeleteLogAction implements Action {
    private Storage storage;

    public void setStorage(Storage storage){
        this.storage = storage;
    }

    public String perform(HttpServletRequest req, HttpServletResponse resp) {
        deleteLog(req.getParameter("path"));
        return "";
    }

    public List<String> getPath(String pathString) {
        StringTokenizer stTok = new StringTokenizer(pathString, "/");
        List<String> pathList = new LinkedList<String>();
        while (stTok.hasMoreElements()) {
            pathList.add(0, stTok.nextToken());
        }
        return pathList;
    }

    public void deleteLog(String pathString) {
        List<String> pathList = getPath(pathString);
        String logName =  pathList.get(pathList.size() - 1);
        pathList.remove(pathList.size() - 1);
        storage.deleteLog(pathList.toArray(new String[0]), logName);
    }
}
