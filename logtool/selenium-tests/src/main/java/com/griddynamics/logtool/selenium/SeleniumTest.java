package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class SeleniumTest {

    protected static final Logger logger = LoggerFactory.getLogger(SeleniumTest.class);

    protected WebDriver driver;
    protected String uiHost;
    protected int uiPort;

    public SeleniumTest(String uiHost, int uiPort) {
        this.uiHost = uiHost;
        this.uiPort = uiPort;
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
    }

    public void tearDown() throws Exception {
        driver.quit();
    }

    protected boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected void waitForElementIsPresent(By by, int timeout) throws TimeoutException, InterruptedException {
        for (int second = 0;; second++) {
            if (second >= timeout) {
                throw new TimeoutException("Timeout exceeded");
            }
            try {
                if(isElementPresent(by)) {
                    break;
                }
            } catch (Exception e) {}
            Thread.sleep(1000);
        }
    }
}
