package com.griddynamics.logtool;


import org.junit.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


public class SearchServerImplTest {

    private SearchServerImpl searchServer;
    private File solrDir;

    @Before
    public void init() {
        String path = new File("").getAbsolutePath();
        String fs = System.getProperty("file.separator");
        String solrPath = path + fs + "test_solr";
        solrDir = new File(solrPath);
        int postfix = 0;
        while(solrDir.exists()) {
            postfix ++;
            solrDir = new File(solrPath + postfix);
        }
        solrDir.mkdir();
        searchServer = new SearchServerImpl(solrDir.getAbsolutePath());
        
        Map<String, String> doc = new HashMap<String, String>();
        doc.put("path", "some path");
        doc.put("startIndex", "0");
        doc.put("length", "100");
        searchServer.index(doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectIndex() {
        Map<String, String> incorrectDoc = new HashMap<String, String>();
        incorrectDoc.put("path", "some path");
        searchServer.index(incorrectDoc);
    }

    @Test
    public void testEmptySearch() {
        List<Map<String, String>> result = searchServer.search("path:nonexistent");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testIndexAndSearch() {
        List<Map<String, String>> result = searchServer.search("path:\"some path\"");
        for(Map<String, String> entry : result) {
            assertTrue(entry.get("path").equals("some path"));
            assertTrue(entry.get("startIndex").equals("0"));
            assertTrue(entry.get("length").equals("100"));
        }
    }

    @Test
    public void testDelete() {
        searchServer.delete("path:\"some path\"");
        List<Map<String, String>> result = searchServer.search("path:\"some path\"");
        assertTrue(result.isEmpty());
    }

    @After
    public void terminate() {
        searchServer.shutdown();
        deleteDirectory(solrDir);

    }

    static public boolean deleteDirectory(File path) {
    if(path.exists()) {
        File[] files = path.listFiles();
        for(int i = 0; i < files.length; i ++) {
            if(files[i].isDirectory()) {
                deleteDirectory(files[i]);
            }
            else {
                files[i].delete();
            }
        }
    }
    return(path.delete());
  }
}
