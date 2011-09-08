package com.griddynamics.logtool.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

public class DeletingAlertsFilterTest extends SeleniumTest {

    public DeletingAlertsFilterTest(String uiHost, int uiPort, String tcpHost, int tcpPort, WebDriver webDriver) {
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
        driver.get(uiHost + ":" + uiPort + "/alerts.html");
        waitForElementIsPresent(By.xpath("//div//img[@src='extjs/resources/delete.gif']"), 50).click();
        waitForElementIsPresent(By.xpath("//span[contains(text(), 'Yes')]"), 50).click();
        try {
            driver.findElement(By.xpath("//div[contains(text(), '.*SELENIUM_TEST.*')]"));
            params.clear();
            params.put("action", "alertsAction");
            params.put("filter", ".*SELENIUM_TEST.*");
            params.put("subaction", "removeFilter");
            Utils.httpGet(this.uiHost, this.uiPort, params);
            return false;
        } catch (NoSuchElementException e) {
            return true;
        }
    }
}
