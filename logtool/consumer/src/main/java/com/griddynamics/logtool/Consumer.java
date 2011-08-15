package com.griddynamics.logtool;

import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class Consumer {
    private static final Logger logger = LoggerFactory.getLogger(Log4jEventsHandler.class);
    private Map<Integer, SyslogServer> syslogServers = new ConcurrentHashMap<Integer, SyslogServer>();
    Log4jEventsServer log4jEventsServer;
    private long lastCheckConfFile = 0;


    private int log4jPort = 4444;
    private Storage storage;
    private SearchServer searchServer;

    public void setLog4jPort(int log4jPort) {
        this.log4jPort = log4jPort;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setSearchServer(SearchServer searchServer) {
        this.searchServer = searchServer;
    }

    public void startLog4j() {
        log4jEventsServer = new Log4jEventsServer(log4jPort, storage, searchServer);
        log4jEventsServer.intitialize();
    }

    public void startSyslog(int port, final String regexp, final Map<String, Integer> groups) {
        SyslogServer syslogServer = new SyslogServer(port, regexp, groups, storage, searchServer);
        syslogServer.initialize();
        syslogServers.put(port, syslogServer);
    }

    public void stopSyslogServer(int port) {
        SyslogServer syslogServer = syslogServers.get(port);
        syslogServers.remove(port);
        syslogServer.shutdown();
    }

    public void stopServers() {
        for (int i : syslogServers.keySet()) {
            SyslogServer syslogServer = syslogServers.get(i);
            syslogServer.shutdown();
        }
        log4jEventsServer.shutdown();
    }

    public void startServers() {
        startLog4j();
        try {
            checkForConfFile();
            Map <Integer, SyslogConf> syslogConfMap = readSyslogConf();
            Set<Integer> portSet = syslogConfMap.keySet();
            for(Integer port: portSet){
                startSyslog(port ,syslogConfMap.get(port).getRegexp(),syslogConfMap.get(port).getGroupMap());
            }
        } catch (IOException e) {
            logger.error("udpconf.xml exists and not a file");
        }

        Timer tm = new Timer();
        TimerTask timerTask = new TimerTask(){
            @Override
            public void run() {
                updateSyslogServers();
            }
        };
        tm.schedule(timerTask, 30000, 30000);
    }

    public void updateSyslogServers(){
        Map <Integer, SyslogConf> syslogConfMap = readSyslogConf();
        if(syslogConfMap != null){
            Set<Integer> portSetInConf = syslogConfMap.keySet();
            for(Integer port : portSetInConf){
                if(syslogServers.get(port) == null){
                    startSyslog(port, syslogConfMap.get(port).getRegexp(), syslogConfMap.get(port).getGroupMap());
                    logger.info("New UDP listener added on port" + port);
                } else if(!syslogConfMap.get(port).getRegexp().equals(syslogServers.get(port).getRegexp()) ||
                        !syslogConfMap.get(port).getGroupMap().equals(syslogServers.get(port).getGroups())){
                    stopSyslogServer(port);
                    startSyslog(port, syslogConfMap.get(port).getRegexp(), syslogConfMap.get(port).getGroupMap());
                    logger.info("UDP listener on port " + port + " reconfigured");
                }
            }
            Set<Integer> portsWorking = syslogServers.keySet();
            for(Integer port : portsWorking){
                if(syslogConfMap.get(port) == null){
                    stopSyslogServer(port);
                    logger.info("UDP listener on port " + port + " removed");
                }
            }
        }
    }

    private void checkForConfFile() throws IOException{
        String path = new File("").getAbsolutePath();
        String fs = System.getProperty("file.separator");
        String consumerConfPath = path + fs + "udpconf.xml";
        File udpConfFile = new File(consumerConfPath);

        if(!udpConfFile.exists()){
            InputStream in = Consumer.class.getResourceAsStream(fs + "udpconf.xml");
            OutputStream out = new FileOutputStream(udpConfFile);
            int buf = in.read();
            while(buf != -1){
                out.write(buf);
                buf = in.read();
            }
            out.flush();
            in.close();
            out.close();
        } else if(!udpConfFile.isFile()){
            throw new IOException("udpconf.xml exists and not a file");
        }
    }

    private Map<Integer, SyslogConf> readSyslogConf() {
        Map<Integer ,SyslogConf> confMap = new HashMap<Integer, SyslogConf>();
        String confPath = new File("").getAbsolutePath() +System.getProperty("file.separator") + "udpconf.xml";
        File confFile = new File(confPath);
        if(lastCheckConfFile >= confFile.lastModified()){
            return null;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document doc = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(confFile);
        } catch (ParserConfigurationException e) {
            logger.error(e.getCause().getMessage(), e.getCause());
        } catch (SAXException e) {
            logger.error(e.getCause().getMessage(), e.getCause());
        } catch (IOException e) {
            logger.error(e.getCause().getMessage(), e.getCause());
        }
        NodeList nodeLst = doc.getElementsByTagName("listener");
        for (int s = 0; s < nodeLst.getLength(); s++) {

            Node node = nodeLst.item(s);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                SyslogConf conf = new SyslogConf();
                int port = 0;
                Map<String, Integer> groupMap = new HashMap<String, Integer>();

                NodeList innerList = node.getChildNodes();
                for (int i = 0; i < innerList.getLength(); i++) {
                    Node innerNode = innerList.item(i);
                    if (innerNode.getNodeName().equals("port")) {
                        port = Integer.parseInt(innerNode.getTextContent());
                    }
                    if (innerNode.getNodeName().equals("regexp")) {
                        conf.setRegexp(innerNode.getTextContent());
                    }
                    if (innerNode.getNodeName().equals("groups")) {
                        NodeList groupsList = innerNode.getChildNodes();
                        for (int k = 0; k < groupsList.getLength(); k++) {
                            Node groupNode = groupsList.item(k);
                            if (!groupNode.getNodeName().equals("#text")) {
                                groupMap.put(groupNode.getNodeName(), Integer.parseInt(groupNode.getTextContent()));
                            }
                        }
                        conf.setGroupMap(groupMap);
                    }
                }
                confMap.put(port,conf);
            }
        }
        lastCheckConfFile = confFile.lastModified();
        return confMap;
    }

    class SyslogConf {
        private int port;
        private String regexp;
        private Map<String, Integer> groupMap;

        public String getRegexp() {
            return regexp;
        }

        public void setRegexp(String regexp) {
            this.regexp = regexp;
        }

        public Map<String, Integer> getGroupMap() {
            return groupMap;
        }

        public void setGroupMap(Map<String, Integer> groupMap) {
            this.groupMap = groupMap;
        }
    }
}