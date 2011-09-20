package com.griddynamics.logtool;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@Path("/rest/")
public class LogRest {
    public Storage storage;
    public SearchServer searchServer;

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setSearchServer(SearchServer searchServer) {
        this.searchServer = searchServer;
    }

    @GET
    @Path("/logstructure/")
    @Produces({"application/xml", "application/octet-stream"})
    public Node getAppList() {
        Node node = new Node();
        Tree FileTree = storage.getTree(-1);
        List<String> strucList = new LinkedList<String>();
        strucList.addAll(FileTree.getChildren().keySet());
        node.setFolder(strucList);
        List<String> fileList = new LinkedList<String>();
        fileList.addAll(storage.getTree(0, new String[0]).getChildren().keySet());
        node.setFile(fileList);
        return node;

    }

    @GET
    @Path("/logstructure/{all:.*}")
    @Produces({"application/xml", "application/octet-stream"})
    public Node getAppList(@PathParam("all") String all, @Context HttpServletResponse response, @QueryParam("pageNumber") String pageNumber, @QueryParam("pageLength") String pageLength) {
        StringTokenizer st = new StringTokenizer(all, "/");
        List<String> pathList = new ArrayList<String>();
        while (st.hasMoreElements()) {
            pathList.add(st.nextToken());
        }
        String[] path = pathList.toArray(new String[0]);
        if (storage.checkIfExists(path)) {
            if (storage.checkIsFile(path)) {
                String filename = path[path.length - 1];
                response.setContentType("APPLICATION/OCTET-STREAM");
                String disHeader = "Attachment;Filename =\"" + filename + "\"";
                response.setHeader("Content-Disposition", disHeader);
                path = Arrays.copyOf(path, path.length - 1);
                try {
                    if (pageNumber == null) {
                        long length = storage.getLogLength(path, filename);
                        int pageSize = 2500;
                        for (int i = 0; i < Math.ceil(length / (double) pageSize); i++) {
                            storage.getLogNew(path, filename, i * pageSize, pageSize, response.getOutputStream());
                        }
                    } else {
                        int pageSize = Integer.parseInt(pageLength);
                        int pageNum = Integer.parseInt(pageNumber);
                        storage.getLogNew(path, filename, pageNum * pageSize, pageSize, response.getOutputStream());
                    }
                    response.getOutputStream().close();
                } catch (Exception e) {
                }
                return null;
            } else {
                Node node = new Node();
                List<String> strucList = new ArrayList<String>();
                strucList.addAll(getStructureLevel(path));
                node.setFolder(strucList);
                List<String> fileList = new ArrayList<String>();
                fileList.addAll(storage.getTree(0, path).getChildren().keySet());
                node.setFile(fileList);
                return node;
            }
        } else {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.type("text/html");
            builder.entity("<h3>File Not Found</h3>");
            throw new WebApplicationException(builder.build());
        }
    }

    private Set<String> getStructureLevel(String[] path) {
        if (path.length < 3) {
            if (path.length == 1) {
                return storage.getTree(-1).getChildren().get(path[0]).getChildren().keySet();
            } else if (path.length == 2) {
                return storage.getTree(-1).getChildren().get(path[0]).getChildren().get(path[1]).getChildren().keySet();
            }
        }
        return new HashSet<String>();
    }

    @GET
    @Path("/filter/")
    @Produces("application/xml")
    public Filter getFilterList() {
        List<String> filters = new LinkedList<String>();
        filters.addAll(storage.getSubscribers().keySet());
        Filter filter = new Filter();
        filter.setFilter(filters);
        return filter;
    }

    @GET
    @Path("/filter/{id}")
    @Produces("application/xml")
    public Alert getFilterList(@PathParam("id") String id) {
        List<String> alerts = new LinkedList<String>();
        alerts.addAll(storage.getAlerts().get(id));
        if (alerts.size() == 0) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.type("text/html");
            builder.entity("<h3>Filter " + id + "doesn't exists</h3>");
            throw new WebApplicationException(builder.build());
        }
        Alert alert = new Alert();
        alert.setAlert(alerts);
        return alert;
    }

}

@XmlRootElement(name = "Level")
class Node {
    List<String> folder;
    List<String> file;

    @XmlElement(name = "Folder")
    public List<String> getFolder() {
        return folder;
    }

    public void setFolder(List<String> folder) {
        this.folder = folder;
    }

    @XmlElement(name = "File")
    public List<String> getFile() {
        return file;
    }

    public void setFile(List<String> file) {
        this.file = file;
    }
}

@XmlRootElement(name = "Filters")
class Filter {
    List<String> filter;


    @XmlElement(name = "Filter")
    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }

}

@XmlRootElement(name = "Alerts")
class Alert {
    List<String> alert;

    @XmlElement(name = "Alert")
    public List<String> getAlert() {
        return alert;
    }

    public void setAlert(List<String> alert) {
        this.alert = alert;
    }
}



