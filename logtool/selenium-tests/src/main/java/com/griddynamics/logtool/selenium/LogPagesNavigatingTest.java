package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

public class LogPagesNavigatingTest extends SeleniumTest {
    public LogPagesNavigatingTest(String uiHost, int uiPort, String tcpHost, int tcpPort, WebDriver webDriver) {
        super(uiHost, uiPort, tcpHost, tcpPort, webDriver);
    }

    public boolean perform() throws Exception {
        Utils.tcpSend(tcpHost, tcpPort, "Selenium test message", "Selenium.LogPagesNavigationTest", 400, 10);

        driver.get(uiHost + ":" + uiPort);
        try {
            clickLogAfterInstance("LogPagesNavigationTest");
        } catch (NoSuchElementException e){
            logger.error("Unable to find test log");
            return false;
        }

        String text = driver.findElement(By.xpath("//div[@id='div2']")).getText();
        int curPage = Integer.parseInt(text.substring(12, text.indexOf(" from")));

        driver.findElement(By.xpath("//span[contains(text(), 'Previous')]")).click();
        text = driver.findElement(By.xpath("//div[@id='div2']")).getText();
        int prevPage = Integer.parseInt(text.substring(12, text.indexOf(" from")));

        driver.findElement(By.xpath("//span[contains(text(), 'Next')]")).click();
        text = driver.findElement(By.xpath("//div[@id='div2']")).getText();
        int nextPage = Integer.parseInt(text.substring(12, text.indexOf(" from")));

        Utils.deleteDirectory(uiHost, uiPort, "Selenium");

        return curPage == nextPage && prevPage == curPage - 1;
    }
}
