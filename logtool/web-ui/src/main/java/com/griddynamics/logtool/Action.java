package com.griddynamics.logtool;

/**
 * Created by IntelliJ IDEA.
 * User: slivotov
 * Date: Jul 12, 2011
 * Time: 9:00:55 PM
 * To change this template use File | Settings | File Templates.
 */
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Action {
     public String perform(HttpServletRequest request, HttpServletResponse response);
}
