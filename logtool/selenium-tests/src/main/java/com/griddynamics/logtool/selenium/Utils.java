package com.griddynamics.logtool.selenium;

import com.griddynamics.logtool.agents.UDPSendler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URI;

import java.util.*;


public class Utils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormat.forPattern("yyyy-dd-MMM").withLocale(Locale.ENGLISH);
    private static String fs = System.getProperty("file.separator");
    

    public static String reversePath(String path) {
        String[] pathSegments = path.split(fs);
        StringBuilder res = new StringBuilder();
        for (int i = pathSegments.length - 1; i >= 0; i--) {
            res.append(pathSegments[i]).append(fs);
        }
        return res.toString();
    }

    public static List<String> getPathSegments(String pathString) {
        StringTokenizer stTok = new StringTokenizer(pathString, fs);
        List<String> pathList = new LinkedList<String>();
        while (stTok.hasMoreElements()) {
            pathList.add(stTok.nextToken());
        }
        return pathList;
    }

    public static String getPath(List<String> segments) {
        StringBuilder out = new StringBuilder();
        for(String s : segments) {
            out.append(s).append(fs);
        }
        out.append(FORMATTER.print(System.currentTimeMillis())).append(".log");
        return out.toString();
    }

    public static String getFilename() {
        return FORMATTER.print(System.currentTimeMillis()) + ".log";
    }

    public static String createPath(String application, String host, String instance) {
        return getFilename() + "/" + instance + "/" + host + "/" + application;
    }

    public static void tcpSend(String host, int port, String message, String application, int amount, long delay)
            throws Exception {
        Logger logger = Logger.getLogger(Utils.class);
        SocketAppender appender = new SocketAppender(host, port);
        appender.setApplication(application);
        logger.addAppender(appender);
        for(int i = 0; i < amount; i ++) {
            logger.info(message + " " + i);
            Thread.sleep(delay);
        }
        appender.close();
    }

    public static void udpSend (String host, int port, String message, String application, String instance, int amount,
                         long delay) throws Exception {
        UDPSendler sendler = new UDPSendler(host, port);
        StringBuilder sb = new StringBuilder();
        String delim = "|";
        sb.append(delim).append(application);
        sb.append(delim).append(instance);
        sb.append(delim).append(DATE_TIME_FORMATTER.print(System.currentTimeMillis()));
        sb.append(delim).append(message);
        message = sb.toString();
        for(int i = 0; i < amount; i ++) {
            sendler.sendMsg(message + " " + i);
            Thread.sleep(delay);
        }
    }

    public static String httpGet(String host, int port, Map<String, String> params) throws Exception {
        String out = null;
        List<NameValuePair> qParams = new ArrayList<NameValuePair>();
        for(String s : params.keySet()) {
            qParams.add(new BasicNameValuePair(s, params.get(s)));
        }
        URI uri = URIUtils.createURI("http", host + ":" + port, -1, "/logtool",
                URLEncodedUtils.format(qParams, "UTF-8"), null);

        HttpGet httpget = new HttpGet(uri);
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpget);

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream inputStream = entity.getContent();
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            out = sb.toString();
        }
        return out;
    }

    public static void deleteDirectory(String host, int port, String path) throws Exception {
        String reversedPath = reversePath(path);

        Map<String, String> params = new HashMap<String, String>();

        params.put("action", "deleteDirectory");
        params.put("path", reversedPath);
        httpGet(host, port, params);
    }
}