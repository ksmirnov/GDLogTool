package com.griddynamics.logtool;

import java.util.HashMap;
import java.util.Map;

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
        } else {
            throw new RuntimeException(" was unable to find an action named '" + actionName + "'.");
        }
    }
}
