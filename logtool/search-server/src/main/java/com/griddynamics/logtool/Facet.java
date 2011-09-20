package com.griddynamics.logtool;

import java.util.HashMap;
import java.util.Map;

public class Facet {

    private String name;
    private Map<String, Long> count = new HashMap<String, Long>();


    public Facet(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Long> getCount() {
        return count;
    }

    public void setCount(Map<String, Long> count) {
        this.count = count;
    }

    public void addCount(String key, Long value) {
        count.put(key, value);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}