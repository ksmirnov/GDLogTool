package com.griddynamics.logtool;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        if (/*size - count <= partToView ||*/ partToView == -1) {
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
        List<String> pathList = getPath(pathString);
        String logName = pathList.get(pathList.size() - 1);
        pathList.remove(pathList.size() - 1);
        long size = 0;
        try {
            size = storage.getLogLength(pathList.toArray(new String[0]), logName);
            PartToView ptw = getLogToView(size, partToView, count);
            sos.print("{'success': true, ");
            sos.print("'partViewed': '" + ptw.getPartToView() + "', ");
            sos.print("'total': '" + size + " ', 'log': '");
            JSONOutputStream jos = new JSONOutputStream(sos);
            storage.getLogNew(pathList.toArray(new String[0]), logName, ptw.getPartToView(), ptw.getLogToView(), jos);
            sos.print("'}");
            sos.flush();
        } catch (IOException e) {
            sos.print("{'success': false}");
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

    static class JSONOutputStream extends FilterOutputStream {

        private StringBuilder sb = new StringBuilder();

        public JSONOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int b) throws IOException {
            sb.setLength(0);
            switch (b) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\'':
                    sb.append("\\\'");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    char ch = (char) b;
                    if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                        String ss = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            sb.append('0');
                        }
                        sb.append(ss.toUpperCase());
                    } else {
                        sb.append(ch);
                    }
            }
            for(int i = 0; i < sb.length(); i ++) {
                out.write(sb.charAt(i));
            }
        }
    }
}

