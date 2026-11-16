<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    com.studentmanagement.beans.User user = (com.studentmanagement.beans.User) session.getAttribute("user");
    if (user != null) {
        if (user.isStaff()) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
        }
    } else {
        response.sendRedirect(request.getContextPath() + "/login");
    }
%>
