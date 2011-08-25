package com.griddynamics.logtool;

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

/**
 * File def
 *
 * @author ksmirnov
 */
public class PerformanceAction extends Action{

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
                    appendSimpleChild(doc, channel, "startTime", String.valueOf(perf.getStartTime()));
                    appendSimpleChild(doc, channel, "endTime", String.valueOf(perf.getEndTime()));
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
