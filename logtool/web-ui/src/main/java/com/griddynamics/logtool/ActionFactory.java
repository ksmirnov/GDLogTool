package com.griddynamics.logtool;

import java.util.HashMap;
import java.util.Map;

public class ActionFactory {

    public Action create(String actionName) {
        if (actionName.equalsIgnoreCase("getTree")) {
            return new TreeAction();
        } else if (actionName.equalsIgnoreCase("getLog")) {
            return new LogAction();
        } else {
            throw new RuntimeException(" was unable to find an action named '" + actionName + "'.");
        }
    }
}