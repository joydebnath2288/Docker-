package com.studentmanagement.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.filter.AuthFilter;
import com.studentmanagement.service.UserService;
import com.studentmanagement.service.UserServiceImpl;
import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.util.PasswordUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthServletAndFilterTest {

    private UserService userService;
    private AuthServlet authServlet;
    private AuthFilter authFilter;
    private FrontControllerServlet frontController;

    @BeforeEach
    public void setUp() {
        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public User findByUsername(String username) {
                if ("admin".equals(username)) {
                    return new User(1, "admin", PasswordUtil.hashPassword("adminPass"), 1, 1);
                } else if ("student".equals(username)) {
                    return new User(2, "student", PasswordUtil.hashPassword("studentPass"), 2, 1);
                } else if ("inactive".equals(username)) {
                    return new User(3, "inactive", PasswordUtil.hashPassword("inactivePass"), 2, 0);
                }
                return null;
            }
        };

        StudentDAO mockStudentDAO = new StudentDAO() {
            @Override
            public Student getStudentByUsername(String username) {
                if ("student".equals(username)) {
                    Student s = new Student();
                    s.setUsername("student");
                    return s;
                }
                return null;
            }
        };

        userService = new UserServiceImpl(mockUserDAO, mockStudentDAO);
        authServlet = new AuthServlet(userService);
        authFilter = new AuthFilter(userService);
        frontController = new FrontControllerServlet(null, userService);
    }

    @Test
    public void testScenarioA_AuthServletAdminLoginRedirectsToAdminDashboard() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/login", "POST");
        ctx.parameters.put("username", "admin");
        ctx.parameters.put("password", "adminPass");

        authServlet.doPost(ctx.getRequest(), ctx.getResponse());

        assertEquals("/context/admin/dashboard", ctx.redirectUrl);
        assertNotNull(ctx.session);
        User sessionUser = (User) ctx.session.getAttribute("user");
        assertNotNull(sessionUser);
        assertEquals(1, sessionUser.getRoleId());
    }

    @Test
    public void testScenarioB_AuthServletStudentLoginRedirectsToStudentDashboard() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/login", "POST");
        ctx.parameters.put("username", "student");
        ctx.parameters.put("password", "studentPass");

        authServlet.doPost(ctx.getRequest(), ctx.getResponse());

        assertEquals("/context/student/dashboard", ctx.redirectUrl);
        assertNotNull(ctx.session);
        User sessionUser = (User) ctx.session.getAttribute("user");
        assertNotNull(sessionUser);
        assertEquals(2, sessionUser.getRoleId());
    }

    @Test
    public void testScenarioE_AuthServletInactiveLoginDeniedNoSession() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/login", "POST");
        ctx.parameters.put("username", "inactive");
        ctx.parameters.put("password", "inactivePass");

        authServlet.doPost(ctx.getRequest(), ctx.getResponse());

        assertNull(ctx.redirectUrl);
        assertEquals("/login.jsp", ctx.forwardedUrl);
        assertEquals("Account is inactive. Please contact administrator.", ctx.attributes.get("errorMessage"));
        assertNull(ctx.session);
    }

    @Test
    public void testScenarioJ_AuthServletLogoutInvalidatesSession() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/logout", "GET");
        ctx.createSession();
        ctx.session.setAttribute("user", new User(1, "admin", "pass", 1, 1));

        authServlet.doGet(ctx.getRequest(), ctx.getResponse());

        assertTrue(ctx.sessionInvalidated);
        assertEquals("/context/login", ctx.redirectUrl);
    }

    @Test
    public void testAuthFilterBlocksUnauthenticatedUser() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/students", "GET");
        MockFilterChain chain = new MockFilterChain();

        authFilter.doFilter(ctx.getRequest(), ctx.getResponse(), chain);

        assertFalse(chain.called);
        assertEquals("/context/login", ctx.redirectUrl);
    }

    @Test
    public void testAuthFilterBlocksStudentFromAdminRoute() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/add-student", "GET");
        ctx.createSession();
        ctx.session.setAttribute("user", new User(2, "student", "pass", 2, 1));
        MockFilterChain chain = new MockFilterChain();

        authFilter.doFilter(ctx.getRequest(), ctx.getResponse(), chain);

        assertFalse(chain.called);
        assertEquals("/context/student/dashboard", ctx.redirectUrl);
    }

    @Test
    public void testAuthFilterAllowsAdminToAdminRoute() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/add-student", "GET");
        ctx.createSession();
        ctx.session.setAttribute("user", new User(1, "admin", "pass", 1, 1));
        MockFilterChain chain = new MockFilterChain();

        authFilter.doFilter(ctx.getRequest(), ctx.getResponse(), chain);

        assertTrue(chain.called);
    }

    @Test
    public void testFrontControllerGetStudentsRedirectsAdminToAdminDashboard() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/students", "GET");
        ctx.createSession();
        ctx.session.setAttribute("user", new User(1, "admin", "adminPass", 1, 1));

        frontController.doGet(ctx.getRequest(), ctx.getResponse());

        assertEquals("/context/admin/dashboard", ctx.redirectUrl);
    }

    @Test
    public void testFrontControllerGetStudentsRedirectsStudentToStudentDashboard() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/students", "GET");
        ctx.createSession();
        ctx.session.setAttribute("user", new User(2, "student", "studentPass", 2, 1));

        frontController.doGet(ctx.getRequest(), ctx.getResponse());

        assertEquals("/context/student/dashboard", ctx.redirectUrl);
    }

    @Test
    public void testFrontControllerStudentBlockedFromAdminDashboard() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/admin/dashboard", "GET");
        ctx.createSession();
        ctx.session.setAttribute("user", new User(2, "student", "studentPass", 2, 1));

        frontController.doGet(ctx.getRequest(), ctx.getResponse());

        assertEquals("/context/student/dashboard", ctx.redirectUrl);
    }

    @Test
    public void testFrontControllerAdminAccessesAdminDashboard() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/admin/dashboard", "GET");
        ctx.createSession();
        ctx.session.setAttribute("user", new User(1, "admin", "adminPass", 1, 1));

        frontController.doGet(ctx.getRequest(), ctx.getResponse());

        assertEquals("/students.jsp", ctx.forwardedUrl);
        assertEquals(Boolean.TRUE, ctx.attributes.get("isAdmin"));
    }

    @Test
    public void testFrontControllerStudentAccessesStudentDashboard() throws Exception {
        TestRequestContext ctx = new TestRequestContext("/student/dashboard", "GET");
        ctx.createSession();
        ctx.session.setAttribute("user", new User(2, "student", "studentPass", 2, 1));

        frontController.doGet(ctx.getRequest(), ctx.getResponse());

        assertEquals("/students.jsp", ctx.forwardedUrl);
        assertEquals(Boolean.FALSE, ctx.attributes.get("isAdmin"));
    }

    private static class MockFilterChain implements FilterChain {
        private boolean called = false;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            called = true;
        }
    }

    private static class TestRequestContext {
        private final String servletPath;
        private final String method;
        private final Map<String, String> parameters = new HashMap<>();
        private final Map<String, Object> attributes = new HashMap<>();
        private final Map<String, Object> sessionAttributes = new HashMap<>();

        private HttpSession session;
        private boolean sessionInvalidated = false;
        private String redirectUrl;
        private String forwardedUrl;
        private int status = 200;
        private final StringWriter stringWriter = new StringWriter();

        public TestRequestContext(String servletPath, String method) {
            this.servletPath = servletPath;
            this.method = method;
        }

        public void createSession() {
            session = (HttpSession) Proxy.newProxyInstance(
                    HttpSession.class.getClassLoader(),
                    new Class<?>[]{HttpSession.class},
                    (proxy, method1, args) -> {
                        String name = method1.getName();
                        if ("getAttribute".equals(name)) return sessionAttributes.get(args[0]);
                        if ("setAttribute".equals(name)) { sessionAttributes.put((String) args[0], args[1]); return null; }
                        if ("removeAttribute".equals(name)) { sessionAttributes.remove(args[0]); return null; }
                        if ("invalidate".equals(name)) { sessionInvalidated = true; session = null; sessionAttributes.clear(); return null; }
                        return null;
                    }
            );
        }

        public HttpServletRequest getRequest() {
            return (HttpServletRequest) Proxy.newProxyInstance(
                    HttpServletRequest.class.getClassLoader(),
                    new Class<?>[]{HttpServletRequest.class},
                    (proxy, method1, args) -> {
                        String name = method1.getName();
                        if ("getServletPath".equals(name)) return servletPath;
                        if ("getMethod".equals(name)) return method;
                        if ("getContextPath".equals(name)) return "/context";
                        if ("getRequestURI".equals(name)) return "/context" + servletPath;
                        if ("getParameter".equals(name)) return parameters.get(args[0]);
                        if ("getAttribute".equals(name)) return attributes.get(args[0]);
                        if ("setAttribute".equals(name)) { attributes.put((String) args[0], args[1]); return null; }
                        if ("getHeader".equals(name)) return null;
                        if ("getContentType".equals(name)) return null;
                        if ("setCharacterEncoding".equals(name)) return null;
                        if ("getSession".equals(name)) {
                            if (args == null || args.length == 0 || Boolean.TRUE.equals(args[0])) {
                                if (session == null) createSession();
                                return session;
                            } else {
                                return session;
                            }
                        }
                        if ("getRequestDispatcher".equals(name)) {
                            String target = (String) args[0];
                            return (RequestDispatcher) Proxy.newProxyInstance(
                                    RequestDispatcher.class.getClassLoader(),
                                    new Class<?>[]{RequestDispatcher.class},
                                    (p, m, a) -> {
                                        if ("forward".equals(m.getName())) {
                                            forwardedUrl = target;
                                        }
                                        return null;
                                    }
                            );
                        }
                        return null;
                    }
            );
        }

        public HttpServletResponse getResponse() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class},
                    (proxy, method1, args) -> {
                        String name = method1.getName();
                        if ("sendRedirect".equals(name)) { redirectUrl = (String) args[0]; return null; }
                        if ("setStatus".equals(name)) { status = (Integer) args[0]; return null; }
                        if ("getStatus".equals(name)) return status;
                        if ("setContentType".equals(name)) return null;
                        if ("setCharacterEncoding".equals(name)) return null;
                        if ("getWriter".equals(name)) return new PrintWriter(stringWriter);
                        return null;
                    }
            );
        }
    }
}
