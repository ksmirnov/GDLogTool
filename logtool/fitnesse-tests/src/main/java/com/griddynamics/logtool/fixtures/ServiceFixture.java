package com.griddynamics.logtool.fixtures;

import fitlibrary.DoFixture;


public class ServiceFixture extends DoFixture {

    public  void sleepFor(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted");
        }
    }
}
