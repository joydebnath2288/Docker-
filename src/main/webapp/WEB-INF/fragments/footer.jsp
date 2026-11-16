<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <footer class="footer">
        <div class="container">
            <p class="mb-0">Student Management System &bull; <c:choose><c:when test="${not empty sessionScope.user}">Single-Module Servlet &amp; jQuery REST API</c:when><c:otherwise>Secured</c:otherwise></c:choose></p>
        </div>
    </footer>
