package com.griddynamics.logtool;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import static com.griddynamics.logtool.PathConstructor.getPath;

public class LogAction extends Action {
    public void perform(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        getLog(req.getParameter("path"),
                Integer.parseInt(req.getParameter("partToView")),
                Integer.parseInt(req.getParameter("lines")), resp.getOutputStream());
    }

    public PartToView getLogToView(long size, long partToView, int count) {
        PartToView ptw = new PartToView();
        int logToView = count;
        if (size - count <= partToView || partToView == -1) {
            partToView = size - count;
        }
        if (size < count) {
            logToView = (int) size;
            partToView = 0;
        }
        ptw.setLogToView(logToView);
        ptw.setPartToView(partToView);
        return ptw;
    }

    public void getLog(String pathString, int partToView, int count, ServletOutputStream sos) throws IOException {
        sos.print("response = ");
        List<String> pathList = getPath(pathString);
        String logName = pathList.get(pathList.size() - 1);
        pathList.remove(pathList.size() - 1);
        long size = 0;
        try {
            size = storage.getLogLength(pathList.toArray(new String[0]), logName);
            PartToView ptw = getLogToView(size, partToView, count);
            sos.print("{ 'partViewed': '" + ptw.getPartToView() + "' ,");
            sos.print(" 'total' : '" + size + " ' , 'log' : '");
            storage.getLogNew(pathList.toArray(new String[0]), logName, ptw.getPartToView(), ptw.getLogToView(), sos);
            sos.print(" '}");
            sos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class PartToView {
        public int getLogToView() {
            return logToView;
        }

        public void setLogToView(int logToView) {
            this.logToView = logToView;
        }

        public long getPartToView() {
            return partToView;
        }

        public void setPartToView(long partToView) {
            this.partToView = partToView;
        }

        private long partToView;
        private int logToView;
    }
}
