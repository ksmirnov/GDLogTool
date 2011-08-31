package com.griddynamics.logtool.fixtures;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogtoolRequester {

    private String host;
    private int port;

    public LogtoolRequester(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String get(Map<String, String> params) throws Exception {
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
}
