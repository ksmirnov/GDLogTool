package com.griddynamics.logtool.selenium;

import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;

import java.util.HashMap;
import java.util.Map;

public class GetSubscribersListTest extends SeleniumTest {
    public GetSubscribersListTest(String uiHost, int uiPort, String tcpHost, int tcpPort, WebDriver webDriver) {
        super(uiHost, uiPort, tcpHost, tcpPort, webDriver);
    }

    public boolean perform() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "alertsAction");
        params.put("subaction", "subscribe");
        params.put("filter", ".*FATAL.*");
        params.put("email", "fake@fake.fake");
        Utils.httpGet(uiHost, uiPort, params);

        Utils.tcpSend(tcpHost, tcpPort, "Selenium FATAL test message", "Selenium.GetSubscribersListTest", 1, 10);

        driver.get(uiHost + ":" + uiPort);

        driver.findElement(By.xpath("//span[contains(text(), 'Alerts')]")).click();
        boolean res = false;

        for (int second = 0; ; second++) {
            if (second >= 60) {
                res = false;
                break;
            }
            if (isElementPresent(By.xpath("//div[contains(text(), '.*FATAL.*')]"))) {
                Selenium selenium = new WebDriverBackedSelenium(driver, uiHost + ":" + uiPort);
                selenium.open("/alerts.html");
                selenium.doubleClick("xpath=//div[contains(text(), '.*FATAL.*')]");
                break;
            }
            Thread.sleep(1000);
        }

        for (int second = 0; ; second++) {
            if (second >= 60) {
                res = false;
                break;
            }
            if (isElementPresent(By.xpath("//div[contains(text(), 'fake@fake.fake')]"))) {
                res = true;
                break;
            }
            Thread.sleep(1000);
        }
        params.clear();
        params.put("action", "alertsAction");
        params.put("filter", ".*FATAL.*");
        params.put("subaction", "removeFilter");
        Utils.httpGet(uiHost, uiPort, params);
        Utils.deleteDirectory(uiHost, uiPort, "Selenium");
        return res;
    }
}
