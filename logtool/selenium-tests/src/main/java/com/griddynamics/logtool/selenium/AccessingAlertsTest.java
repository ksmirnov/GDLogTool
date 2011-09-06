package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;


public class AccessingAlertsTest extends SeleniumTest {

    public AccessingAlertsTest(String uiHost, int uiPort, WebDriver webDriver) {
        super(uiHost, uiPort, webDriver);
    }

    public boolean perform(String tcpHost, int tcpPort) throws Exception {
        boolean out;
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "alertsAction");
        params.put("subaction", "subscribe");
        params.put("filter", ".*SELENIUM_TEST.*");
        params.put("email", "selenium@some.mail");
        Utils.httpGet(this.uiHost, this.uiPort, params);
        Utils.tcpSend(tcpHost, tcpPort, "SELENIUM_TEST", "Selenium.AlertsTest", 1, 0);
        driver.get(uiHost + ":" + uiPort + "/alerts.html");
        String xpath = "//div[starts-with(@id, 'gridview-')]/table/tbody/tr/td/div[contains(text(),'SELENIUM_TEST')]";
        waitForElementIsPresent(By.xpath(xpath), 60).click();
        if(driver.findElements(By.xpath(xpath)).size() > 1) {
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
