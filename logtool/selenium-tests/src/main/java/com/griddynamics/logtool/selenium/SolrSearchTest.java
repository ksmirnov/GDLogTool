package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

public class SolrSearchTest extends SeleniumTest {
    public SolrSearchTest(String uiHost, int uiPort, String tcpHost, int tcpPort, WebDriver webDriver) {
        super(uiHost, uiPort, tcpHost, tcpPort, webDriver);
    }

    public boolean perform() {
        try {
            Utils.tcpSend(tcpHost, tcpPort, "Selenium fast message", "SeleniumTestApp.SeleniumInst", 10, 100);
            Thread.sleep(100);
            driver.get(uiHost + ":" + uiPort);
            driver.findElement(By.xpath("//span[contains(text(), 'Search')]")).click();
            driver.findElement(By.xpath("//input[@role='textbox']")).sendKeys("fast");
            driver.findElement(By.xpath("//div[@class='x-toolbar x-window-item x-toolbar-footer " +
                    "x-docked x-docked-bottom x-toolbar-docked-bottom x-toolbar-footer-docked-bottom " +
                    "x-box-layout-ct']//span[contains(text(), 'Search')]")).click();
            waitForElementIsPresent(By.xpath("//div[@id='searchResGrid']//table//tr[1]"), 50);
            int tr = 2;
            while(true){
                try{
                    driver.findElement(By.xpath("//div[@id='searchResGrid']//table//tr[" + tr + "]"));
                    tr++;
                } catch (NoSuchElementException e){
                    break;
                }
            }
            Utils.deleteDirectory(uiHost, uiPort, "SeleniumTestApp");
            if(tr == 12) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
