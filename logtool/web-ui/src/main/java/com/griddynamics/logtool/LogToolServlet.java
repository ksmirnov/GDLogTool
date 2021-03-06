package com.griddynamics.logtool;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class LogToolServlet extends HttpServlet {

    private ActionFactory factory = new ActionFactory();

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

 	WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        Action action = factory.create(req.getParameter("action"),(Storage)ctx.getBean("fileStorage"),
                (SearchServer)ctx.getBean("searchServer"),
                (Consumer)ctx.getBean("consumer"));
        action.perform(req, resp);
    }
}
