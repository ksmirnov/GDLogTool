package com.griddynamics.logtool.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public class SeleniumTest {

    protected static final Logger logger = LoggerFactory.getLogger(SeleniumTest.class);

    protected WebDriver driver;
    protected String uiHost;
    protected int uiPort;

    public SeleniumTest(String uiHost, int uiPort, WebDriver webDriver) {
        this.uiHost = uiHost;
        this.uiPort = uiPort;
        driver = webDriver;
    }

    protected boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected void  clickLogAfterInstance(String instance) throws InterruptedException {
        this.waitForElementIsPresent(By.xpath("//div[starts-with(@id, 'treeview-')]"), 60);
        WebElement treeview = driver.findElement(By.xpath("//div[starts-with(@id, 'treeview-')]"));
        int tr = 1;
            for(;;tr ++) {
                treeview.findElement(By.xpath("//table/tbody/tr[" + tr + "]"));
                try {
                    treeview.findElement(By.xpath("//table/tbody/tr[" + tr + "]/td/div[contains(text(), '" + instance + "')]"));
                    tr ++;
                    break;
                } catch(NoSuchElementException e) {
                    continue;
                }
            }
            treeview.findElement(By.xpath("//table/tbody/tr[" + tr + "]/td/div/input")).click();
    }

    protected WebElement waitForElementIsPresent(By by, int timeout) throws TimeoutException, InterruptedException {
        for (int second = 0;; second++) {
            if (second >= timeout) {
                throw new TimeoutException("Timeout exceeded! Unable to find requested element.");
            }
            try {
                if(isElementPresent(by)) {
                    return driver.findElement(by);
                }
            } catch (Exception e) {}
            Thread.sleep(1000);
        }
    }
}
