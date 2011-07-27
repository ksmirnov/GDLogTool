package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Action {
    public Storage storage;

    public void setStorage(Storage storage){
        this.storage = storage;
    }

    public abstract String perform(HttpServletRequest request, HttpServletResponse response);
}
