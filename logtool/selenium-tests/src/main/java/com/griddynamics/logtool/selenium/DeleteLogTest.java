package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;


public class DeleteLogTest extends SeleniumTest {

    public DeleteLogTest(String uiHost, int uiPort) {
        super(uiHost, uiPort);
    }

    public boolean perform(String tcpHost, int tcpPort) throws Exception {
        Utils.tcpSend(tcpHost, tcpPort, "This message should be deleted", "AppToDel.InstanceToDel", 10, 100);
        this.driver.get(uiHost + ":" + uiPort);
        this.waitForElementIsPresent(By.xpath("//div[starts-with(@id, 'treeview-')]"), 60);
        WebElement treeview = driver.findElement(By.xpath("//div[starts-with(@id, 'treeview-')]"));
        int tr = 1;
        try {
            for(;;tr ++) {
                treeview.findElement(By.xpath("//table/tbody/tr[" + tr + "]"));
                try {
                    treeview.findElement(By.xpath("//table/tbody/tr[" + tr + "]/td/div[contains(text(), 'InstanceToDel')]"));
                    tr ++;
                    break;
                } catch(NoSuchElementException e) {
                    continue;
                }
            }
            treeview.findElement(By.xpath("//table/tbody/tr[" + tr + "]/td/div/input")).click();
            driver.findElement(By.xpath("//span[contains(text(), 'Delete')]")).click();
            this.waitForElementIsPresent(By.xpath("//button/span[contains(text(),'Yes')]"), 60).click();
            this.driver.get(uiHost + ":" + uiPort);
            this.waitForElementIsPresent(By.xpath("//div[starts-with(@id, 'treeview-')]"), 60);
            return (!this.isElementPresent(By.xpath("//div[contains(text(), 'AppToDel')]")) &&
                    !isElementPresent(By.xpath("//div[contains(text(), 'InstanceToDel')]")));
        } catch(NoSuchElementException e) {
            logger.error("Unable to find test log");
            return false;
        }
    }
    
}
