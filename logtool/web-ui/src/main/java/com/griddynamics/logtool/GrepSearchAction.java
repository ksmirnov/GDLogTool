package com.griddynamics.logtool;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;

import static com.griddynamics.logtool.PathConstructor.getPath;

public class GrepSearchAction extends Action {
    public void perform(HttpServletRequest req, HttpServletResponse resp) {
        try {
            doSearch(req.getParameter("path"), req.getParameter("searchRequest"), Integer.parseInt(req.getParameter("pageSize")), resp.getOutputStream());
        } catch (IOException ex) {

        }
    }

    public void doSearch(String pathString, String searchRequest, int pageSize, ServletOutputStream sos) {
        try {
            sos.print(storage.doSearch(getPath(pathString).toArray(new String[0]), searchRequest, pageSize).toString());
        } catch (IOException ex) {

        }
    }
}
