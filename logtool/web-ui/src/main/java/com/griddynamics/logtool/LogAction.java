package com.griddynamics.logtool;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: slivotov
 * Date: Jul 12, 2011
 * Time: 9:04:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogAction implements Action {


    public String perform(HttpServletRequest req, HttpServletResponse resp) {
        return getLog(req.getParameter("path"),
                Integer.parseInt(req.getParameter("partToView")),
                Integer.parseInt(req.getParameter("lines")));
    }

    public String getLog(String path, int partToView, int count) {
        Storage storage = Consumer.fileStorage;
        String logName = path.substring(0, path.indexOf("/"));
        path = path.substring(path.indexOf("/"), path.length());
        List<String> pathList = new LinkedList<String>();
        while (path.length() != 0) {
            String folder = path.substring(path.lastIndexOf("/") + 1, path.length());
            pathList.add(folder);
            path = path.substring(0, path.lastIndexOf("/"));
        }
        List<String> logList = storage.getLog(pathList.toArray(new String[0]), logName);
        String log = "";
        int logToView = count;

        if (logList.size() <= partToView) {
            logToView = logList.size() + count - partToView;
            partToView = logList.size();
            if (logToView == 0) {
                logToView = count;
            }
            log = partToView + "***";
        }

        if (partToView == -1) {
            partToView = logList.size();
            log = partToView + "***";
        }
        for (int i = 0; i < logToView; i++) {
            log = log + "<br>" + logList.get(partToView - i - 1);
        }

        log = logList.size() + ":" + log;
        return log;
    }
}
