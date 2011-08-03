package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class Action {
    public Storage storage;

    public void setStorage(Storage storage){
        this.storage = storage;
    }

    public abstract void perform(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
