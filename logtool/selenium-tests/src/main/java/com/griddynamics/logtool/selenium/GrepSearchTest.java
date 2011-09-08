package com.griddynamics.logtool.selenium;

import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;


public class GrepSearchTest extends SeleniumTest {
    
    public GrepSearchTest(String uiHost, int uiPort, String tcpHost, int tcpPort, WebDriver webDriver) {
        super(uiHost, uiPort, tcpHost, tcpPort, webDriver);
    }

    public boolean perform() throws Exception {
        Utils.tcpSend(tcpHost, tcpPort, "Selenium fast message", "SeleniumTestApp.SeleniumInst", 10, 100);
        Thread.sleep(100);
        driver.get(uiHost + ":" + uiPort);
        clickLogAfterInstance("SeleniumInst");
        driver.findElement(By.xpath("//span[contains(text(), 'Search')]")).click();
        driver.findElement(By.xpath("//input[@role='textbox']")).sendKeys("grep: fast");
        driver.findElement(By.xpath("//div[@class='x-toolbar x-window-item x-toolbar-footer " +
                "x-docked x-docked-bottom x-toolbar-docked-bottom x-toolbar-footer-docked-bottom " +
                "x-box-layout-ct']//span[contains(text(), 'Search')]")).click();
        waitForElementIsPresent(By.xpath("//div[@id='searchResGrid']//table//tr[2]//td[2]//div"), 50);
        Selenium selenium = new WebDriverBackedSelenium(driver, uiHost + ":" + uiPort);
        int count = Integer.parseInt(selenium.getText("xpath=//div[@id='searchResGrid']//table//tr[2]//td[2]//div"));
        Utils.deleteDirectory(uiHost, uiPort, "SeleniumTestApp");
        if(count == 10) {
            return true;
        } else {
            return false;
        }
    }

}
