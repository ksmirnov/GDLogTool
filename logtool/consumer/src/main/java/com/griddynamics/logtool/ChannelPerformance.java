package com.griddynamics.logtool;

public class ChannelPerformance {

    private int recieved;
    private volatile long startTime;
    private volatile long endTime;
    private int exceptions;
    private int averageLatency;

    public int getAverageLatency() {
        return averageLatency;
    }

    public void setAverageLatency(int averageLatency) {
        this.averageLatency = averageLatency;
    }

    public int getRecieved() {
        return recieved;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
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