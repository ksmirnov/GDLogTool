package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.griddynamics.logtool.PathConstructor.getPath;

public class DeleteDirectoryAction extends Action {
    public String perform(HttpServletRequest req, HttpServletResponse resp) {
        deleteDirectory(req.getParameter("path"));
        return "";
    }

    public void deleteDirectory(String pathString) {
        storage.deleteDirectory(getPath(pathString).toArray(new String[0]));
    }
}
