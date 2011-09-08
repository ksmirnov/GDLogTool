package com.griddynamics.logtool;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.util.Map;

public class PerformanceAction extends Action{

    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS");

    @Override
    public void perform(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(request.getParameter("subaction").equalsIgnoreCase("get")) {
            Map<String, ChannelPerformance> statement = consumer.getPerformance();
            if(statement == null) {
                response.getWriter().println("There are no performance results found!");
                response.getWriter().println("Check if test mode enabled.");
            }
            try {
                DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
                Document doc = docBuilder.newDocument();
                Element root = doc.createElement("channels");
                doc.appendChild(root);
                for(String s : statement.keySet()) {
                    ChannelPerformance perf = statement.get(s);
                    Element channel = doc.createElement("channel");
                    channel.setAttribute("name", s);
                    root.appendChild(channel);
                    appendSimpleChild(doc, channel, "recieved", String.valueOf(perf.getRecieved()));
                    appendSimpleChild(doc, channel, "firstReceived", formatter.print(perf.getFirstReceived()));
                    appendSimpleChild(doc, channel, "lastReceived", formatter.print(perf.getLastRecieved()));
                    appendSimpleChild(doc, channel, "exceptions", String.valueOf(perf.getExceptions()));
                    appendSimpleChild(doc, channel, "averageLatency", String.valueOf(perf.getAverageLatency()));
                }
                TransformerFactory transfac = TransformerFactory.newInstance();
                Transformer trans = transfac.newTransformer();
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(response.getOutputStream());
                trans.transform(source, result);
            } catch(Exception e) {
                response.getWriter().println("An error occurred while creating xml output");
            }
        } else if(request.getParameter("subaction").equalsIgnoreCase("reset")) {
            consumer.resetStatement();
        }
    }

    private Element appendSimpleChild(Document doc, Element node, String name, String content) {
        Element el = doc.createElement(name);
        el.setTextContent(content);
        node.appendChild(el);
        return el;
    }
}
