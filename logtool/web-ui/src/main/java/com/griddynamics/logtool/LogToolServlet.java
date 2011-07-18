package com.griddynamics.logtool;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class LogToolServlet extends HttpServlet {

    private ActionFactory factory = new ActionFactory();
    

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Action action = factory.create(req.getParameter("action"));
        String response = action.perform(req, resp);
        PrintWriter out = resp.getWriter();
        out.println( response );
        out.close();


    }
}
