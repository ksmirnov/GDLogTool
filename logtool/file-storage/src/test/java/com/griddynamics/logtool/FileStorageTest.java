package com.griddynamics.logtool;

import org.junit.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit test for simple filestorage.
 */
public class FileStorageTest {
    private static final String[] path1 = {"app", "host", "inst1", "", null, "day", null, "hour"};
    private static final String[] path2 = {"app", "host", "inst2", "day", "hour"};
    private static final String[] path3 = {"app", "host1", "inst", "day", "hour"};
    private static final String[] path4 = {"app1", "host"};

    private static final String logDate1 = "2011-12-12T03:09:53";
    private static final String logDate2 = "2011-5-5T12:35:46";

    private static final String logName1 = "2011-12-Dec.log";
    private static final String logName2 = "2011-05-May.log";

    private static final String logMsg = "log";

    private static final String logFolder = "TestLogs/";

    private FileStorage fileStorage;

    @Before
    public void initialize() {
        BeanFactory factory = new ClassPathXmlApplicationContext("fileStorageConfiguration.xml");
        fileStorage = (FileStorage) factory.getBean("fileStorage");
        fileStorage.setLogFolder(logFolder);
    }

    @Test
    public void simpleAddAndDeleteTest() {
        fileStorage.addMessage(path4, logDate2, logMsg);
        fileStorage.deleteLog(path4, logName2);

        assertFalse(new File(logFolder + path4[0]).exists());
    }

    @Test
    public void appendAndGetTest() {
        fileStorage.addMessage(path1, logDate1, logMsg);
        fileStorage.addMessage(path1, logDate1, logMsg);

        List<String> expectedList = new ArrayList<String>();
        expectedList.add("03:09:53 " + logMsg);
        expectedList.add("03:09:53 " + logMsg);

        assertEquals(expectedList, fileStorage.getLog(path1, logName1));

        fileStorage.deleteLog(path1, logName1);
    }

    @Test
    public void incorrectDataTest() {
        fileStorage.addMessage(path1, logDate1, logMsg);

        fileStorage.getLog(path1, logName2);

        fileStorage.deleteLog(path1, logName2);

        Tree tree = fileStorage.getTree(1, path2);

        assertEquals(new Tree().getChildren(), tree.getChildren());

        fileStorage.addMessage(path1, "date", "msg");
        fileStorage.deleteLog(path1, "default.log");

        fileStorage.addMessage(new String[0], "2000-12-12", "");
        fileStorage.deleteLog(new String[0], "2000-12-Dec.log");
    }

    @Test
    public void getTreeAndCreateTreeFromDiskTest() {
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
        expectedSet1.add(path1[5]);
        expectedSet1.add(path2[3]);
        expectedSet2.add(path1[7]);
        expectedSet3.add(path2[4]);
        assertEquals(expectedSet1, subTree.getChildren().keySet());
        assertEquals(expectedSet2, subTree.getChildren().get(path1[5]).getChildren().keySet());
        assertEquals(expectedSet3, subTree.getChildren().get(path2[3]).getChildren().keySet());
        assertEquals(null, subTree.getChildren().get(path1[5]).getChildren().get(path1[4]));

        fileStorage.deleteDirectory(path1);
        fileStorage.deleteDirectory(path2);
        fileStorage.deleteDirectory(path3);
        fileStorage.deleteDirectory(path4);
    }

    @Test
    public void deleteTest() {
        fileStorage.addMessage(path1, logDate1, logMsg);
        fileStorage.addMessage(path2, logDate1, logMsg);
        fileStorage.addMessage(path3, logDate1, logMsg);
        fileStorage.addMessage(path4, logDate1, logMsg);
        fileStorage.addMessage(path1, logDate2, logMsg);
        fileStorage.addMessage(path2, logDate2, logMsg);
        fileStorage.addMessage(path3, logDate2, logMsg);

        String[] deletePath = new String[2];
        deletePath[0] = path1[0];
        deletePath[1] = path1[1];
        fileStorage.deleteDirectory(deletePath);
        Tree allTree = fileStorage.getTree(-1);
        Tree node = allTree.getChildren().get(path1[0]);
        assertEquals(null, node.getChildren().get(path1[1]));

        String[] clearPath = new String[1];
        clearPath[0] = path1[0];
        fileStorage.deleteDirectory(clearPath);
        clearPath[0] = path4[0];
        fileStorage.deleteDirectory(clearPath);
        assertEquals(0, new File(logFolder).list().length);
    }

    @Test
    public void wipeTest() {
        long size = (((long) 1 << 20) + 15) / 5;
        StringBuffer sb = new StringBuffer();
        for (long i = 0; i < size; i++) {
            sb.append("qwerty");
        }

        fileStorage.addMessage(path1, logDate1, sb.toString());
        fileStorage.addMessage(path1, logDate1, "wipe!");

        List<String> expectedList2 = new ArrayList<String>();
        expectedList2.add("03:09:53 wipe!");

        assertEquals(expectedList2, fileStorage.getLog(path1, logName1));

        fileStorage.deleteLog(path1, logName1);
    }


    //It's commented for not spaming emails every time...
    /*@Test
    public void emailNotificationTest() {
        String filter = "a*b";
        fileStorage.subscribe(filter, "gdlogtool@gmail.com");
        fileStorage.addMessage(path1, logDate1, "aaaab");
        fileStorage.addMessage(path1, logDate1, "aaabbb");
        fileStorage.removeFilter(filter);
        fileStorage.subscribe(filter, "gdlogtool@gmail.com");
        fileStorage.unsubscribe(filter, "gdlogtool@gmail.com");
        fileStorage.addMessage(path1, logDate1, "aaaab");
        fileStorage.deleteDirectory(path1);
    }*/

    @Test
    public void alertsStorageTest() {
        String filter = "a*b";
        fileStorage.subscribe(filter, "gdlogtool@gmail.com");
        fileStorage.addMessage(path1, logDate1, "aaaab");
        fileStorage.addMessage(path1, logDate1, "aaaaaaaaab");
        fileStorage.addMessage(path1, logDate1, "aaabbb");

        Set<String> expectedSet1 = new HashSet<String>();
        Set<String> expectedSet2 = new HashSet<String>();

        expectedSet1.add(filter);
        expectedSet2.add("03:09:53 aaaab");
        expectedSet2.add("03:09:53 aaaaaaaaab");

        assertEquals(expectedSet1, fileStorage.getAlerts().keySet());
        assertEquals(expectedSet2, fileStorage.getAlerts().get(filter));

        fileStorage.removeAlert("a*b", "03:09:53 aaaab");
        expectedSet2.remove("03:09:53 aaaab");

        assertEquals(expectedSet2, fileStorage.getAlerts().get(filter));

        fileStorage.deleteLog(path1, logName1);
    }


    @After
    public void deleteLogFolder() {
        File folder = new File(logFolder);
        deleteDirectory(folder);
    }

    private boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                if (!deleteDirectory(new File(dir.getAbsolutePath() + File.separatorChar +  child))) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
