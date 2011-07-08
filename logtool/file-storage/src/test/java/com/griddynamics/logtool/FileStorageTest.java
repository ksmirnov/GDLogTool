package com.griddynamics.logtool;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.*;

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

        String[] path1 = {"app", "host", "inst1", "day", "hour"};
        String[] path2 = {"app", "host", "inst2", "day", "hour"};
        String[] path3 = {"app", "host1", "inst", "day", "hour"};
        String[] path4 = {"app1", "host"};

        String logDate1 = "2011-12-12";
        String logDate2 = "2011-5-5";

        String logName1 = "2011-12-Dec.log";
        String logName2 = "2011-5-May.log";

        String logMsg = "log";


        //Simple add-delete test
        fileStorage.addMessage(path4, logDate2, logMsg);
        fileStorage.deleteLog(path4, logName2);
        assertFalse(new File("logs/" + path4[0]).exists());

        //Append test and getLog test
        fileStorage.addMessage(path1, logDate1, logMsg);
        fileStorage.addMessage(path1, logDate1, logMsg);
        List<String> expectedList = new ArrayList<String>();
        expectedList.add(logMsg + logMsg);
        assertEquals(expectedList, fileStorage.getLog(path1, logName1));
        fileStorage.deleteLog(path1, logName1);

        //Incorrect get log, delete log, get tree
        fileStorage.addMessage(path1, logDate1, logMsg);
        fileStorage.getLog(path1, logName2);
        fileStorage.deleteLog(path1, logName2);
        Tree tree = fileStorage.getTree(1, path2);
        assertEquals(new Tree().getChildren(), tree.getChildren());


        //getTree test
        fileStorage.addMessage(path1, logDate1, logMsg);
        fileStorage.addMessage(path2, logDate1, logMsg);
        fileStorage.addMessage(path3, logDate1, logMsg);
        fileStorage.addMessage(path4, logDate1, logMsg);
        fileStorage.addMessage(path1, logDate2, logMsg);
        fileStorage.addMessage(path2, logDate2, logMsg);
        fileStorage.addMessage(path3, logDate2, logMsg);

        Tree allTree = fileStorage.getTree(-1);
        assertEquals(2, allTree.getChildren().keySet().size());
        assertEquals(2, allTree.getChildren().get(path1[0]).getChildren().get(path1[1]).getChildren().keySet().size());
        assertEquals(null, allTree.getChildren().get(path4[0]).getChildren().get(path4[1]));

        Tree fileNames = fileStorage.getTree(0, path1);
        Tree expectedTree = new Tree();
        expectedTree.getChildren().put(logName1, null);
        expectedTree.getChildren().put(logName2, null);
        assertEquals(expectedTree.getChildren(), fileNames.getChildren());

        Tree subTree = fileStorage.getTree(1, Arrays.copyOfRange(path1, 0, 3));
        Set<String> expectedSet1 = new HashSet<String>();
        Set<String> expectedSet2 = new HashSet<String>();
        Set<String> expectedSet3 = new HashSet<String>();
        expectedSet1.add(path1[3]);
        expectedSet1.add(path2[3]);
        expectedSet2.add(path1[4]);
        expectedSet3.add(path2[4]);
        assertEquals(expectedSet1, subTree.getChildren().keySet());
        assertEquals(expectedSet2, subTree.getChildren().get(path1[3]).getChildren().keySet());
        assertEquals(expectedSet3, subTree.getChildren().get(path2[3]).getChildren().keySet());
        assertEquals(null, subTree.getChildren().get(path1[3]).getChildren().get(path1[4]));

        allTree.getChildren().clear();
        fileStorage.createTreeFromDisk();
        allTree = fileStorage.getTree(-1);
        subTree = fileStorage.getTree(1, Arrays.copyOfRange(path1, 0, 3));
        assertEquals(2, allTree.getChildren().keySet().size());
        assertEquals(2, allTree.getChildren().get(path1[0]).getChildren().get(path1[1]).getChildren().keySet().size());
        assertEquals(null, allTree.getChildren().get(path4[0]).getChildren().get(path4[1]));
        assertEquals(expectedSet1, subTree.getChildren().keySet());
        assertEquals(expectedSet2, subTree.getChildren().get(path1[3]).getChildren().keySet());
        assertEquals(expectedSet3, subTree.getChildren().get(path2[3]).getChildren().keySet());
        assertEquals(null, subTree.getChildren().get(path1[3]).getChildren().get(path1[4]));

        //Delete test
        String[] deletePath = new String[2];
        deletePath[0] = path1[0];
        deletePath[1] = path1[1];
        fileStorage.deleteLog(deletePath);
        allTree = fileStorage.getTree(-1);
        Tree node = allTree.getChildren().get(path1[0]);
        assertEquals(null, node.getChildren().get(path1[1]));

        String[] clearPath = new String[1];
        clearPath[0] = path1[0];
        fileStorage.deleteLog(clearPath);
        clearPath[0] = path4[0];
        fileStorage.deleteLog(clearPath);
        assertEquals(0, new File("logs").list().length);

        //Wipe test
        long size = (((long) 1 << 20) + 15) / 5;
        StringBuffer sb = new StringBuffer();
        for (long i = 0; i < size; i++) {
            sb.append("qwerty");
        }
        fileStorage.addMessage(path1, logDate1, sb.toString());
        fileStorage.addMessage(path1, logDate1, "wipe!");
        List<String> expectedList2 = new ArrayList<String>();
        expectedList2.add("wipe!");
        assertEquals(expectedList2, fileStorage.getLog(path1, logName1));
        fileStorage.deleteLog(path1, logName1);
    }
}
