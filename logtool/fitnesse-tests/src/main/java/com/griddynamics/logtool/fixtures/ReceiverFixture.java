package com.griddynamics.logtool.fixtures;

import fit.ActionFixture;
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
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ReceiverFixture extends ActionFixture {
    public String receiveMessage() {
        try {
            List<NameValuePair> qParams = new ArrayList<NameValuePair>();
            qParams.add(new BasicNameValuePair("action", "getlog"));
            qParams.add(new BasicNameValuePair("path", "2011-15-Aug.log/Some/localhost/Grinder"));
            qParams.add(new BasicNameValuePair("partToView", "-1"));
            qParams.add(new BasicNameValuePair("lines", "2500"));
            URI uri = URIUtils.createURI("http", "localhost:8088", -1, "/logtool", URLEncodedUtils.format(qParams, "UTF-8"), null);

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

                String res = sb.toString();
                int index = res.indexOf("'log' : '");

                return res.substring(index + 9, res.length() - 3);
            }

            return "Entity is null";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
