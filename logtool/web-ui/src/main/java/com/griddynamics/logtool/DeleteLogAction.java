package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.griddynamics.logtool.PathConstructor.getPath;

public class DeleteLogAction extends Action {
    public String perform(HttpServletRequest req, HttpServletResponse resp) {
        deleteLog(req.getParameter("path"));
        return "";
    }

    public void deleteLog(String pathString) {
        List<String> pathList = getPath(pathString);
        String logName =  pathList.get(pathList.size() - 1);
        pathList.remove(pathList.size() - 1);
        storage.deleteLog(pathList.toArray(new String[0]), logName);
    }
}
