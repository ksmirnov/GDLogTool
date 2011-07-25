package com.griddynamics.logtool;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class LogAction implements Action {


    public String perform(HttpServletRequest req, HttpServletResponse resp) {
        return "response = " + getLog(req.getParameter("path"),
                Integer.parseInt(req.getParameter("partToView")),
                Integer.parseInt(req.getParameter("lines")));
    }

    public String getLog(String pathString, int partToView, int count) {
        BeanFactory springFactory = new ClassPathXmlApplicationContext("fileStorageConfiguration.xml");
        Storage storage = (Storage) springFactory.getBean("fileStorage");
        StringTokenizer stTok = new StringTokenizer(pathString, "/");
        String logName = stTok.nextToken();
        List<String> pathList = new LinkedList<String>();
        while(stTok.hasMoreElements()){
            pathList.add(0,stTok.nextToken());
        }
        List<String> logList = storage.getLog(pathList.toArray(new String[0]), logName);
        StringBuilder log = new StringBuilder("");
        int logToView = count;

        if (logList.size() <= partToView) {
            logToView = logList.size() + count - partToView;
            partToView = logList.size();
            if (logToView == 0) {
                logToView = count;
            }
        } 
        if (partToView == -1) {
            partToView = logList.size();
        }
        log.append("{ 'partViewed': '").append(partToView).append("' ,");
        log.append(" 'total' : '").append(logList.size()).append(" ' , 'log' : '");
        for (int i = 0; i < logToView; i++) {
            log.append("<br>").append(logList.get(partToView - i - 1));
        }
        log.append(" '}");
        return log.toString();
    }
}
