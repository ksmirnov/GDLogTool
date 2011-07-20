package com.griddynamics.logtool;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreeAction implements Action {

    public String perform(HttpServletRequest request, HttpServletResponse response) {
        BeanFactory springFactory = new ClassPathXmlApplicationContext("fileStorageConfiguration.xml");
        Storage storage = (Storage) springFactory.getBean("fileStorage");
        Tree FileTree = storage.getTree(-1);

        return getJson(FileTree);
    }

    protected static String getJson(Tree FileTree) {
        Map<String, Tree> tree = FileTree.getChildren();
        List<String> path = new LinkedList<String>();
        return "[ " + getJsonFromMap(path, tree) + " ]";

    }

    protected static String getJsonFromMap(List path, Map<String, Tree> tree) {
        StringBuilder output = new StringBuilder("");
        Set<String> keySet = tree.keySet();
        if (!keySet.isEmpty()) {
            for (String s : keySet) {
                Tree tempTree = tree.get(s);
                if (tempTree != null) {
                    String folderDown = "";
                    output.append("{text:'").append(s).append("', expanded: true, children: [");
                    Map<String, Tree> tempMap = tempTree.getChildren();
                    path.add(s);
                    folderDown = getJsonFromMap(path, tempMap);
                    path.remove(path.size() - 1);
                    output.append(folderDown).append("] },");
                } else {
                    output.append("{text:'").append(s).append("', expanded: true, children: [");
                    path.add(s);
                    String[] pathArray = new String[path.size()];
                    path.toArray(pathArray);
                    path.remove(path.size() - 1);
                    BeanFactory springFactory = new ClassPathXmlApplicationContext("fileStorageConfiguration.xml");
                    Storage storage = (Storage)springFactory.getBean("fileStorage");
                    Set<String> logsSet = storage.getTree(0, pathArray).getChildren().keySet();
                    for (String logName : logsSet) {
                        output.append("{text: '").append(logName).append("',leaf:true},");
                    }
                    output.delete( output.length() - 1,output.length()).append(" ] },");
                }

            }
            output.append(" ");
        }
        return output.toString();
    }
}