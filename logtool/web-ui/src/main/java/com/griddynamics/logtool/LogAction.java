package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.griddynamics.logtool.PathConstructor.getPath;

public class LogAction extends Action {
    public String perform(HttpServletRequest req, HttpServletResponse resp) {
        return "response = " + getLog(req.getParameter("path"),
                Integer.parseInt(req.getParameter("partToView")),
                Integer.parseInt(req.getParameter("lines")));
    }

    public PartToView getLogToView(List<String> logList,int partToView, int count){
        PartToView ptw = new PartToView();
        int logToView = count;
        if (partToView == -1) {
            partToView = logList.size();
        }
        if (logList.size() < partToView) {
            partToView = logList.size();
        }
        if(logList.size() < count){
            logToView = logList.size();
        }
        ptw.setLogToView(logToView);
        ptw.setPartToView(partToView);
        return ptw;

    }

    public String getLog(String pathString, int partToView, int count) {
        List<String> pathList = getPath(pathString);
        String logName =  pathList.get(pathList.size()-1);
        pathList.remove(pathList.size()-1);
        List<String> logList = storage.getLog(pathList.toArray(new String[0]), logName);
        StringBuilder log = new StringBuilder("");
        PartToView ptw = getLogToView(logList,partToView,count);
        log.append("{ 'partViewed': '").append(ptw.getPartToView()).append("' ,");
        log.append(" 'total' : '").append(logList.size()).append(" ' , 'log' : '");
        for (int i = 0; i < ptw.getLogToView(); i++) {
            log.append("<br>").append(logList.get(ptw.getPartToView() - i - 1));
        }
        log.append(" '}");
        return log.toString();
    }
    
    class PartToView{
        public int getLogToView() {
            return logToView;
        }

        public void setLogToView(int logToView) {
            this.logToView = logToView;
        }

        public int getPartToView() {
            return partToView;
        }

        public void setPartToView(int partToView) {
            this.partToView = partToView;
        }

        private int partToView;
        private int logToView;
    }
}
