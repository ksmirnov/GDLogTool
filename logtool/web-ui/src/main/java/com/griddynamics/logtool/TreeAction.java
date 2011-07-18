package com.griddynamics.logtool;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: slivotov
 * Date: Jul 12, 2011
 * Time: 9:03:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TreeAction implements Action {

    public String perform(HttpServletRequest request, HttpServletResponse response) {
        Storage storage = Consumer.fileStorage;
        Tree FileTree = storage.getTree(-1);

        return getJson(FileTree);
    }

    protected static String getJson(Tree FileTree) {
        Map<String, Tree> tree = FileTree.getChildren();
        List<String> path = new LinkedList<String>();
        return "[ " + getJsonFromMap(path, tree) + " ]";

    }

    protected static String getJsonFromMap(List path, Map<String, Tree> tree) {
        String output = "";
        Set<String> keySet = tree.keySet();
        if (!keySet.isEmpty()) {
            for (String s : keySet) {
                Tree tempTree = tree.get(s);
                if (tempTree != null) {
                    String folderDown = "";
                    output = output + "{text:'" + s + "', expanded: true, children: [";
                    Map<String, Tree> tempMap = tempTree.getChildren();
                    path.add(s);
                    folderDown = getJsonFromMap(path, tempMap);
                    path.remove(path.size() - 1);
                    output = output + folderDown + "] },";
                } else {
                    output = output + "{text:'" + s + "', expanded: true, children: [";
                    path.add(s);
                    String[] pathArray = new String[path.size()];
                    path.toArray(pathArray);
                    path.remove(path.size() - 1);
                    Storage storage = Consumer.fileStorage;
                    Set<String> logsSet = storage.getTree(0, pathArray).getChildren().keySet();
                    for (String logName : logsSet) {
                        output = output + "{text: '" + logName + "',leaf:true},";
                    }
                    output = output.substring(0, output.length() - 1) + " ] },";
                }

            }
        } else {

        }
        output = output.substring(0, output.length()) + " ";
        return output;
    }
}