package com.griddynamics.logtool;

public class ChannelPerformance {

    private int recieved;
    private volatile long firstReceived;
    private volatile long lastRecieved;
    private int exceptions;
    private volatile long averageLatency;

    public long getAverageLatency() {
        return averageLatency;
    }

    public void setAverageLatency(long averageLatency) {
        this.averageLatency = averageLatency;
    }

    public int getRecieved() {
        return recieved;
    }

    public long getFirstReceived() {
        return firstReceived;
    }

    public void setFirstReceived(long firstReceived) {
        this.firstReceived = firstReceived;
    }

    public long getLastRecieved() {
        return lastRecieved;
    }

    public void setLastRecieved(long lastRecieved) {
        this.lastRecieved = lastRecieved;
    }

    public int getExceptions() {
        return exceptions;
    }

    public void addException() {
        exceptions++;
    }

    public void addRecieved(int qty) {
        recieved += qty;
    }
}