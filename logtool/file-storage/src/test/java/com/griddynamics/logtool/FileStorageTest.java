package com.griddynamics.logtool;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Unit test for simple filestorage.
 */
public class FileStorageTest {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageTest.class);

    @Test
    public void testFileStorage() {
        BeanFactory factory = new ClassPathXmlApplicationContext("fileStorageConfiguration.xml");
        FileStorage fileStorage = (FileStorage) factory.getBean("fileStorage");
        String[] path1 = {"app1", "host1", "inst1", "day1", "hour1"};
        String[] path2 = {"app1", "host1", "inst2", "day2", "hour2", "sec3"};
        String[] path3 = {"app2", "host1", "inst1"};
        String[] path4 = {"app2", "host1", "inst1"};
        fileStorage.addMessage(path1, "2004-12-13", "log1\n");
        fileStorage.addMessage(path2, "2005-12-13", "log2\n");
        fileStorage.addMessage(path3, "2004-12-13", "log3\n");
        fileStorage.addMessage(path4, "2004-12-13", "log4\n");
        fileStorage.addMessage(path1, "2006-10-13", "log2\n");
        fileStorage.deleteLog(path4, "2004 13 Dec.log");
        logger.debug(fileStorage.getLog(path1, "2004 13 Dec.log").toString());

        //fileStorage.createTreeFromDisk();

        Tree fs = fileStorage.getTree(-1);
        logger.debug(fs.getChildren().keySet().toString());
        logger.debug(fs.getChildren().get("app1").getChildren().get("host1").getChildren().keySet().toString());
        fs = fileStorage.getTree(0, path1);
        logger.debug(fs.getChildren().keySet().toString());
        fs = fileStorage.getTree(1, "app1");
        Tree node = fs;
        StringBuilder sb = new StringBuilder();
        while (node != null) {
            String key =(String) node.getChildren().keySet().toArray()[0];
            node = node.getChildren().get(key);
            sb.append("/").append(key);
        }
        logger.debug(sb.toString());
        assertTrue(true);
    }
}
