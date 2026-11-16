package com.studentmanagement.filter;

import java.io.IOException;

import com.studentmanagement.beans.User;
import com.studentmanagement.service.UserService;
import com.studentmanagement.service.UserServiceImpl;
import com.studentmanagement.util.JsonUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = {
    "/admin/dashboard",
    "/student/dashboard",
    "/students", 
    "/add-student", 
    "/edit-student", 
    "/delete-student", 
    "/api/students/*", 
    "/api/students"
})
public class AuthFilter implements Filter {

    private final UserService userService;

    public AuthFilter() {
        this(new UserServiceImpl());
    }

    public AuthFilter(UserService userService) {
        this.userService = (userService != null) ? userService : new UserServiceImpl();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (sessionUser == null) {
            reject(req, res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access. Please login.", "/login");
            return;
        }

        User freshUser = userService.findByUsername(sessionUser.getUsername());
        if (freshUser == null || !userService.isActive(freshUser)) {
            if (session != null) {
                session.invalidate();
            }
            reject(req, res, HttpServletResponse.SC_UNAUTHORIZED, "Account is inactive. Please contact administrator.", "/login");
            return;
        }

        session.setAttribute("user", freshUser);

        String path = req.getServletPath();
        if (!isAuthorized(path, freshUser)) {
            reject(req, res, HttpServletResponse.SC_FORBIDDEN, "Access denied. Insufficient permissions for role: " + freshUser.getRoleName(), "/student/dashboard");
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean isAuthorized(String path, User user) {
        if (user == null) {
            return false;
        }
        if (user.isAdmin()) {
            return true;
        }
        if ("/admin/dashboard".equals(path)) {
            return user.isStaff();
        }
        if ("/add-student".equals(path)) {
            return user.isCanAddStudent();
        }
        if ("/edit-student".equals(path)) {
            return user.isCanEditStudent();
        }
        if ("/delete-student".equals(path)) {
            return user.isCanDeleteStudent();
        }
        return true;
    }

    private void reject(HttpServletRequest req, HttpServletResponse res, int status, String msg, String redirectUrl) throws IOException {
        if (JsonUtil.isAjax(req)) {
            res.setStatus(status);
            res.setContentType("application/json; charset=UTF-8");
            res.getWriter().write("{\"success\": false, \"message\": \"" + msg + "\"}");
            res.getWriter().flush();
        } else {
            res.sendRedirect(req.getContextPath() + redirectUrl);
        }
    }

    private boolean isAdminOnlyPath(String path) {
        return "/admin/dashboard".equals(path)
                || "/add-student".equals(path)
                || "/edit-student".equals(path)
                || "/delete-student".equals(path);
    }
}
