package com.griddynamics.logtool;

/**
 * Created by IntelliJ IDEA.
 * User: slivotov
 * Date: Jul 12, 2011
 * Time: 8:58:00 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.HashMap;
import java.util.Map;

public class ActionFactory {

    public Action create(String actionName) {
        if (actionName.equalsIgnoreCase("getTree")) {
            return new TreeAction();
        } else if (actionName.equalsIgnoreCase("getLog")) {
            return new LogAction();
        } else {
            throw new RuntimeException(getClass() + " was unable to find an action named '" + actionName + "'.");
        }
    }
}