package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;


public class AccessingTreeTest extends SeleniumTest {

    public AccessingTreeTest(String uiHost, int uiPort, String tcpHost, int tcpPort, WebDriver webDriver) {
        super(uiHost, uiPort, tcpHost, tcpPort, webDriver);
    }

    public boolean perform() throws Exception {
        Utils.tcpSend(tcpHost, tcpPort, "Selenium fast message", "SeleniumTestApp1.SeleniumInst1", 10, 100);
        Utils.tcpSend(tcpHost, tcpPort, "Selenium fast message", "SeleniumTestApp2.SeleniumInst2", 10, 100);

        Thread.sleep(100);
        driver.get(uiHost + ":" + uiPort);
        try {
            driver.findElement(By.xpath("//table//tbody//td//div[contains(text(), 'SeleniumInst1')]"));
            driver.findElement(By.xpath("//table//tbody//td//div[contains(text(), 'SeleniumInst2')]"));
            driver.findElement(By.xpath("//table//tbody//td//div[contains(text(), 'SeleniumTestApp1')]"));
            driver.findElement(By.xpath("//table//tbody//td//div[contains(text(), 'SeleniumTestApp2')]"));
        } catch (NoSuchElementException e) {
            return false;
        }
        Utils.deleteDirectory(uiHost, uiPort, "SeleniumTestApp1");
        Utils.deleteDirectory(uiHost, uiPort, "SeleniumTestApp2");
        return true;
    }

}
