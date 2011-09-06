package com.griddynamics.logtool.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.concurrent.TimeUnit;

/**
 * Suite class for all UI tests
 */
public class SeleniumSuite {

    public static void main(String[] args) throws Exception {
        WebDriver webDriver = new FirefoxDriver();
        webDriver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        DeleteDirectoryTest ddt = new DeleteDirectoryTest("localhost", 8088, webDriver);
        ddt.perform("localhost", 4444);
        DeleteLogTest dlt = new DeleteLogTest("localhost", 8088, webDriver);
        dlt.perform("localhost", 4444);
        AccessingAlertsTest aat = new AccessingAlertsTest("localhost", 8088, webDriver);
        aat.perform("localhost", 4444);
        MarkingAlertsTest mat = new MarkingAlertsTest("localhost", 8088, webDriver);
        mat.perform("localhost", 4444);
        webDriver.quit();
    }
}
