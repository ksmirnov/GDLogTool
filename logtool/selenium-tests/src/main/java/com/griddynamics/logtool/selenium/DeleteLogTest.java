package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;


public class DeleteLogTest extends SeleniumTest {

    public DeleteLogTest(String uiHost, int uiPort, String tcpHost, int tcpPort, WebDriver webDriver) {
        super(uiHost, uiPort, tcpHost, tcpPort, webDriver);
    }

    public boolean perform() throws Exception {
        Utils.tcpSend(tcpHost, tcpPort, "This message should be deleted", "AppToDel.InstanceToDel", 10, 100);
        driver.get(uiHost + ":" + uiPort);
        try {
            clickLogAfterInstance("InstanceToDel");
        } catch (NoSuchElementException e){
            logger.error("Unable to find test log");
            return false;
        }
        driver.findElement(By.xpath("//span[contains(text(), 'Delete')]")).click();
        waitForElementIsPresent(By.xpath("//button/span[contains(text(),'Yes')]"), 60).click();
        driver.get(uiHost + ":" + uiPort);
        waitForElementIsPresent(By.xpath("//div[starts-with(@id, 'treeview-')]"), 60);
        return (!isElementPresent(By.xpath("//div[contains(text(), 'AppToDel')]")) &&
                !isElementPresent(By.xpath("//div[contains(text(), 'InstanceToDel')]")));
    }
}
