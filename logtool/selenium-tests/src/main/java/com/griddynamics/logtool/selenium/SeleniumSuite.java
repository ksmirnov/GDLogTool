package com.griddynamics.logtool.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * Suite class for all UI tests
 */
public class SeleniumSuite {

    protected static final Logger logger = LoggerFactory.getLogger(SeleniumSuite.class);

    private static String uiHost = "localhost";
    private static int uiPort = 8088;
    private static String tcpHost = "localhost";
    private static int tcpPort = 4444;

    public static void main(String[] args) {
        StringTokenizer st;
        switch(args.length) {
            case 2:
                st = new StringTokenizer(args[1], ":");
                if(st.countTokens() == 2) {
                    tcpHost = st.nextToken();
                    tcpPort = Integer.valueOf(st.nextToken());
                }
            case 1:
                st = new StringTokenizer(args[0], ":");
                if(st.countTokens() == 2) {
                    uiHost = st.nextToken();
                    uiPort = Integer.valueOf(st.nextToken());
                }
        }
        WebDriver webDriver = new FirefoxDriver();
        webDriver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        DeleteLogTest dlt = new DeleteLogTest(uiHost, uiPort, tcpHost, tcpPort, webDriver);
        printResult(dlt);
        DeleteDirectoryTest ddt = new DeleteDirectoryTest(uiHost, uiPort, tcpHost, tcpPort, webDriver);
        printResult(ddt);
        GrepSearchTest gst = new GrepSearchTest(uiHost, uiPort, tcpHost, tcpPort, webDriver);
        printResult(gst);
        SolrSearchTest sst = new SolrSearchTest(uiHost, uiPort, tcpHost, tcpPort, webDriver);
        printResult(sst);
        AccessingAlertsTest aat = new AccessingAlertsTest(uiHost, uiPort, tcpHost, tcpPort, webDriver);
        printResult(aat);
        MarkingAlertsTest mat = new MarkingAlertsTest(uiHost, uiPort, tcpHost, tcpPort, webDriver);
        printResult(mat);
        GetLogTest glt = new GetLogTest(uiHost, uiPort, tcpHost, tcpPort, webDriver);
        printResult(glt);
        LogPagesNavigatingTest lpnt = new LogPagesNavigatingTest(uiHost, uiPort, tcpHost, tcpPort, webDriver);
        printResult(lpnt);
        AutorefreshingLogTest alt = new AutorefreshingLogTest(uiHost, uiPort, tcpHost, tcpPort, webDriver);
        printResult(alt);
        GetSubscribersListTest gslt = new GetSubscribersListTest(uiHost, uiPort, tcpHost, tcpPort, webDriver);
        printResult(gslt);
        webDriver.quit();
    }

    private static void printResult(SeleniumTest test) {
        logger.info("PERFORMING " + test.getClass().getSimpleName() + "...");
        boolean result = false;
        try {
            result = test.perform();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if(result) {
            logger.info("RESULT: SUCCESS");
        } else {
            logger.info("RESULT: FAILED");
        }
    }
}
