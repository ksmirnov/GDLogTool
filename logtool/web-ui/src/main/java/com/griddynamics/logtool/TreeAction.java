package com.griddynamics.logtool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreeAction extends Action {
    public void perform(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Tree FileTree = storage.getTree(-1);
        PrintWriter out = response.getWriter();
        out.println(getJson(FileTree));
        out.close();
    }

    protected String getJson(Tree FileTree) {
        Map<String, Tree> tree = FileTree.getChildren();
        List<String> path = new LinkedList<String>();
        return "[ " + getJsonFromMap(path, tree) + " ]";

    }

    protected String getJsonFromMap(List<String> path, Map<String, Tree> tree) {
        StringBuilder output = new StringBuilder("");
        output.append(getJsonFiles(path));
        Set<String> keySet = tree.keySet();
        if (!keySet.isEmpty()) {
            for (String s : keySet) {
                Tree tempTree = tree.get(s);
                if (tempTree != null) {
                    String folderDown = "";
                    output.append("{text:'").append(s).append("', expanded: true, checked:false, children: [");
                    Map<String, Tree> tempMap = tempTree.getChildren();
                    path.add(s);
                    folderDown = getJsonFromMap(path, tempMap);
                    path.remove(path.size() - 1);
                    output.append(folderDown).append("] },");
                } else {
                    output.append("{text:'").append(s).append("', expanded: true, checked:false, children: [");
                    path.add(s);
                    String[] pathArray = new String[path.size()];
                    path.toArray(pathArray);
                    path.remove(path.size() - 1);
                    Set<String> logsSet = storage.getTree(0, pathArray).getChildren().keySet();
                    for (String logName : logsSet) {
                        output.append("{text: '").append(logName).append("',leaf:true,checked:false},");
                    }
                    output.delete( output.length() - 1,output.length()).append(" ] },");
                }

            }
            output.append(" ");
        }
        return output.toString();
    }

       protected String getJsonFiles(List<String> path){
        Set<String> filesSet = storage.getTree(0,path.toArray(new String[0])).getChildren().keySet();
        if(filesSet.isEmpty()){
            return "";
        }else{
            StringBuilder output = new StringBuilder("");
            for(String file: filesSet){
                output.append("{text: '").append(file).append("',leaf:true,checked:false},");
            }
            return output.toString();
        }
    }
}
