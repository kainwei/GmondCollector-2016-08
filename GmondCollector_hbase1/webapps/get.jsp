<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String [] hosts  = request.getParameterValues("hosts");
    for(int i = 0; i<hosts.length; i++ ){
        out.println(hosts[i]);
    }
    out.println("time is "  + request.getParameter("time") ) ;
    out.println("location is " + request.getAttribute("location"));
%>