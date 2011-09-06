package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

public class AutorefreshingLogTest extends SeleniumTest {
    public AutorefreshingLogTest(String uiHost, int uiPort, String tcpHost, int tcpPort, WebDriver webDriver) {
        super(uiHost, uiPort, tcpHost, tcpPort, webDriver);
    }

    public boolean perform() throws Exception {
        Utils.tcpSend(tcpHost, tcpPort, "Selenium test message", "Selenium.AutorefreshingTest", 1, 10);

        driver.get(uiHost + ":" + uiPort);
        try {
            clickLogAfterInstance("AutorefreshingTest");
        } catch (NoSuchElementException e){
            logger.error("Unable to find test log");
            return false;
        }

        Utils.tcpSend(tcpHost, tcpPort, "Selenium test message", "Selenium.AutorefreshingTest", 1, 10);

        Thread.sleep(5100);

        String text = driver.findElement(By.xpath("//div[@id='div2']")).getText();
        Utils.deleteDirectory(uiHost, uiPort, "Selenium");

        return text.indexOf("Selenium test message", text.indexOf("Selenium test message") + 1) > 0;
    }
}
