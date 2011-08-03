package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static com.griddynamics.logtool.PathConstructor.getPath;

public class DeleteLogAction extends Action {
    public void perform(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        deleteLog(req.getParameter("path"));
        PrintWriter out = resp.getWriter();
        out.println("");
        out.close();;
    }

    public void deleteLog(String pathString) {
        List<String> pathList = getPath(pathString);
        String logName =  pathList.get(pathList.size() - 1);
        pathList.remove(pathList.size() - 1);
        storage.deleteLog(pathList.toArray(new String[0]), logName);
    }
}
