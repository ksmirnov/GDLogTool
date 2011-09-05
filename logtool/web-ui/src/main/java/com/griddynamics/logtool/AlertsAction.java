package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

public class AlertsAction extends Action {

    public static final String JSON_NULL = "[]";
    
    private StringBuilder stringBuilder = new StringBuilder();

    public void perform(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String subaction = req.getParameter("subaction");
        String out = "";
        if(subaction.equalsIgnoreCase("getFilters")) {
            out = getFilters();
        } else if(subaction.equalsIgnoreCase("getEmailAddresses")) {
            out = getEmailAddresses(req.getParameter("filter"));
        } else if(subaction.equalsIgnoreCase("getAlerts")) {
            out = getAlerts(req.getParameter("filter"));
        } else if(subaction.equalsIgnoreCase("removeAlert")) {
            removeAlert(req.getParameter("filter"), req.getParameter("message"));
            out = getAlerts(req.getParameter("filter"));
        } else if(subaction.equalsIgnoreCase("removeFilter")) {
            removeFilter(req.getParameter("filter"));
            out = getFilters();
        } else if (subaction.equalsIgnoreCase("subscribe")) {
            subscribe(req.getParameter("email"), req.getParameter("filter"));
        }
        PrintWriter output = resp.getWriter();
        output.println(out);
        output.close();
    }

    protected void subscribe(String email, String filter) {
        storage.subscribe(filter, email);
    }

    protected String getFilters() {
        Set<String> filters = storage.getSubscribers().keySet();
        if(filters.isEmpty()) {
            return JSON_NULL;
        } else {
            return getJsonFromSet(filters, false);
        }
    }

    protected String getEmailAddresses(String filter) {
        Set<String> emails = storage.getSubscribers().get(filter);
        if(emails == null) {
            return JSON_NULL;
        }
        return getJsonFromSet(emails, false);
    }

    protected String getAlerts(String filter) {
        Set<String> alerts = storage.getAlerts().get(filter);
        if(alerts == null || alerts.isEmpty()) {
            return JSON_NULL;
        } else {
            return getJsonFromSet(alerts, true);
        }
    }

    protected void removeAlert(String filter, String message) {
        storage.removeAlert(filter, message);
    }

    protected void removeFilter(String filter) {
        storage.removeFilter(filter);
    }

    protected String getJsonFromSet(Set<String> set, boolean shortCuts) {
        stringBuilder.setLength(0);
        stringBuilder.append("[");
        for(String s : set) {
            stringBuilder.append("['");
            if(shortCuts == true) {
                if(s.length() > 25) {
                    stringBuilder.append(s.substring(1, 25) + "...', '");
                } else {
                    stringBuilder.append(s.substring(1, s.length() - 1) + "...', '");
                }
            }
            stringBuilder.append(s).append("'],");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
