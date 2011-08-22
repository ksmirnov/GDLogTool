package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class Action {
    public Storage storage;
    public SearchServer searchServer;
    public Consumer consumer;

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public void setStorage(Storage storage){
        this.storage = storage;
    }

    public void setSearchServer(SearchServer searchServer) {
        this.searchServer = searchServer;
    }

    public abstract void perform(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
