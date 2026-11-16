<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <nav class="navbar navbar-expand-lg navbar-dark navbar-custom">
        <div class="container">
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <a class="navbar-brand" href="${pageContext.request.contextPath}${sessionScope.user.staff ? '/admin/dashboard' : '/student/dashboard'}">
                        <i class="bi bi-mortarboard-fill me-2 text-primary"></i>Student Management
                    </a>
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                        <span class="navbar-toggler-icon"></span>
                    </button>
                    <div class="collapse navbar-collapse" id="navbarNav">
                        <ul class="navbar-nav ms-auto align-items-lg-center">
                            <li class="nav-item">
                                <a class="nav-link ${activePage == 'students' ? 'active' : ''}" href="${pageContext.request.contextPath}${sessionScope.user.staff ? '/admin/dashboard' : '/student/dashboard'}">
                                    <i class="bi bi-people me-1"></i>Students
                                </a>
                            </li>
                            <c:if test="${sessionScope.user.canAddStudent}">
                                <li class="nav-item">
                                    <a class="nav-link ${activePage == 'add' ? 'active' : ''}" href="${pageContext.request.contextPath}/add-student">
                                        <i class="bi bi-person-plus me-1"></i>Add Student
                                    </a>
                                </li>
                            </c:if>
                            <li class="nav-item ms-lg-3 d-flex align-items-center">
                                <span class="badge ${sessionScope.user.badgeClass} px-2 py-1 shadow-sm font-monospace" style="font-size: 0.78rem;" title="Logged in as ${sessionScope.user.roleName}">
                                    <i class="bi bi-shield-lock me-1"></i>${sessionScope.user.roleName}
                                </span>
                                <span class="text-white-50 small ms-2 d-none d-md-inline" title="Username">
                                    <i class="bi bi-person-circle me-1"></i>${sessionScope.user.username}
                                </span>
                            </li>
                            <li class="nav-item ms-lg-2">
                                <a class="nav-link btn btn-outline-light btn-sm px-3" href="${pageContext.request.contextPath}/logout">
                                    <i class="bi bi-box-arrow-right me-1"></i>Logout
                                </a>
                            </li>
                        </ul>
                    </div>
                </c:when>
                <c:otherwise>
                    <a class="navbar-brand" href="${pageContext.request.contextPath}/login">
                        <i class="bi bi-mortarboard-fill me-2 text-primary"></i>Student Management
                    </a>
                    <div class="collapse navbar-collapse">
                        <ul class="navbar-nav ms-auto">
                            <li class="nav-item">
                                <a class="nav-link ${activePage == 'login' ? 'active' : ''}" href="${pageContext.request.contextPath}/login">Login</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link ${activePage == 'signup' ? 'active' : ''}" href="${pageContext.request.contextPath}/signup">Sign Up</a>
                            </li>
                        </ul>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </nav>
