package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;


public class DeleteDirectoryTest extends SeleniumTest {
    
    public DeleteDirectoryTest(String uiHost, int uiPort, WebDriver webDriver) {
        super(uiHost, uiPort, webDriver);
    }

    public boolean perform(String tcpHost, int tcpPort) throws Exception {
        Utils.tcpSend(tcpHost, tcpPort, "This message should be deleted", "AppToDel.InstanceToDel", 10, 100);
        this.driver.get(uiHost + ":" + uiPort);
        this.waitForElementIsPresent(By.xpath("//div[contains(text(),'InstanceToDel')]/input"), 60).click();
        driver.findElement(By.xpath("//span[contains(text(), 'Delete')]")).click();
        this.waitForElementIsPresent(By.xpath("//button/span[contains(text(),'Yes')]"), 60).click();
        this.driver.get(uiHost + ":" + uiPort);
        this.waitForElementIsPresent(By.xpath("//div[starts-with(@id, 'treeview-')]"), 60);
        return (!this.isElementPresent(By.xpath("//div[contains(text(), 'AppToDel')]")) &&
                !isElementPresent(By.xpath("//div[contains(text(), 'InstanceToDel')]")));
    }
}
