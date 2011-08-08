package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import static com.griddynamics.logtool.PathConstructor.getPath;

public class DeleteDirectoryAction extends Action {
    public void perform(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        deleteDirectory(req.getParameter("path"));
        PrintWriter out = resp.getWriter();
        out.println("");
        out.close();
    }

    public void deleteDirectory(String pathString) {
        Set<String> files = storage.deleteDirectory(getPath(pathString).toArray(new String[0]));
        for(String file: files) {
            searchServer.delete("path:" + file);
        }
    }
}
