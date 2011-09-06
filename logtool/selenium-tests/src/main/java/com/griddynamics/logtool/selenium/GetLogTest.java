package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

public class GetLogTest extends SeleniumTest {
    public GetLogTest(String uiHost, int uiPort, String tcpHost, int tcpPort, WebDriver webDriver) {
        super(uiHost, uiPort, tcpHost, tcpPort, webDriver);
    }

    public boolean perform() throws Exception {
        Utils.tcpSend(tcpHost, tcpPort, "Selenium test message", "Selenium.GetLogTest", 10, 10);

        driver.get(uiHost + ":" + uiPort);
        try {
            clickLogAfterInstance("GetLogTest");
        } catch (NoSuchElementException e){
            logger.error("Unable to find test log");
            return false;
        }

        String text = driver.findElement(By.xpath("//div[@id='div2']")).getText();
        Utils.deleteDirectory(uiHost, uiPort, "Selenium");

        return text.contains("Selenium test message");
    }
}