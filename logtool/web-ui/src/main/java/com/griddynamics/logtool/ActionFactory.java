package com.griddynamics.logtool;

public class ActionFactory {

    public Action create(String actionName,Storage storage) {
        if (actionName.equalsIgnoreCase("getTree")) {
            TreeAction ta = new TreeAction();
            ta.setStorage(storage);
            return ta;
        } else if (actionName.equalsIgnoreCase("getLog")) {
            LogAction la = new LogAction();
            la.setStorage(storage);
            return la;
        } else if (actionName.equalsIgnoreCase("deleteLog")) {
            DeleteLogAction dla = new DeleteLogAction();
            dla.setStorage(storage);
            return dla;
        } else if (actionName.equalsIgnoreCase("deleteDirectory")) {
            DeleteDirectoryAction dda = new DeleteDirectoryAction();
            dda.setStorage(storage);
            return dda;
        } else if (actionName.equalsIgnoreCase("alertsAction")) {
            AlertsAction aa = new AlertsAction();
            aa.setStorage(storage);
            return aa;
        } else {
            throw new RuntimeException(" was unable to find an action named '" + actionName + "'.");
        }
    }
}
