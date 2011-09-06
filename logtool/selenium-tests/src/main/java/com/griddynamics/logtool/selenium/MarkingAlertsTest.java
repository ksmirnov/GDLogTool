package com.griddynamics.logtool.selenium;

import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;

import java.util.HashMap;
import java.util.Map;


public class MarkingAlertsTest extends SeleniumTest {

    public MarkingAlertsTest(String uiHost, int uiPort, String tcpHost, int tcpPort, WebDriver webDriver) {
        super(uiHost, uiPort, tcpHost, tcpPort, webDriver);
    }

    public boolean perform() throws Exception {
        boolean out;
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "alertsAction");
        params.put("subaction", "subscribe");
        params.put("filter", ".*SELENIUM_TEST.*");
        params.put("email", "selenium@some.mail");
        Utils.httpGet(this.uiHost, this.uiPort, params);
        Utils.tcpSend(tcpHost, tcpPort, "SELENIUM_TEST", "Selenium.AlertsTest", 1, 0);
        driver.get(uiHost + ":" + uiPort + "/alerts.html");
        String fPath = "//div[starts-with(@id, 'gridview-')]/table/tbody/tr/td/div[contains(text(),'SELENIUM_TEST')]";
        waitForElementIsPresent(By.xpath(fPath), 60).click();
        String aPath = "//div[starts-with(@id,'panel-') and contains(.,'Items')]//div[contains(text(),'SELENIUM_TEST')]";
        waitForElementIsPresent(By.xpath(aPath), 60);
        Selenium selenium = new WebDriverBackedSelenium(driver, uiHost + ":" + uiPort);
        selenium.doubleClick("xpath=" + aPath);
        waitForElementIsPresent(By.xpath("//button/span[contains(text(),'Close and remove')]"), 60).click();
        if(driver.findElements(By.xpath(fPath)).size() == 1) {
            out = true;
        } else {
            out = false;
        }
        params.clear();
        params.put("action", "alertsAction");
        params.put("filter", ".*SELENIUM_TEST.*");
        params.put("subaction", "removeFilter");
        Utils.httpGet(this.uiHost, this.uiPort, params);
        Utils.deleteDirectory(uiHost, uiPort, "Selenium");
        return out;
    }
}
