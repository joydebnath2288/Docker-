package com.studentmanagement.controller;

import java.io.IOException;

import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;
import com.studentmanagement.service.UserService;
import com.studentmanagement.service.UserServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/login", "/signup", "/logout"})
public class AuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final UserService userService;

    public AuthServlet() {
        this(new UserServiceImpl());
    }

    public AuthServlet(UserService userService) {
        this.userService = (userService != null) ? userService : new UserServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        HttpSession session = request.getSession(false);

        switch (path) {
            case "/login":
            case "/signup":
                if (session != null && session.getAttribute("user") != null) {
                    User user = (User) session.getAttribute("user");
                    response.sendRedirect(request.getContextPath() + userService.getDashboardRoute(user));
                    return;
                }
                request.setAttribute("currentSemester", com.studentmanagement.util.SemesterUtil.getCurrentSemester());
                request.setAttribute("availableSemesters", com.studentmanagement.util.SemesterUtil.getAvailableSemesters());
                request.setAttribute("availableYears", com.studentmanagement.util.SemesterUtil.getAvailableYears());
                request.getRequestDispatcher("/login".equals(path) ? "/login.jsp" : "/signup.jsp").forward(request, response);
                break;
            case "/logout":
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/login");
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/login".equals(path)) {
            handleLogin(request, response);
        } else if ("/signup".equals(path)) {
            handleSignup(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User user = userService.authenticate(username, password);

        if (user != null) {
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("user", user);

            String destination = userService.getDashboardRoute(user);
            response.sendRedirect(request.getContextPath() + destination);
        } else {
            String error = userService.getLastAuthError();
            request.setAttribute("errorMessage", error != null ? error : "Invalid username or password.");
            request.setAttribute("enteredUsername", username != null ? username.trim() : "");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String department = request.getParameter("department");
        String semester = request.getParameter("semester");
        if (semester == null || semester.trim().isEmpty()) {
            String term = request.getParameter("semesterTerm");
            String year = request.getParameter("semesterYear");
            if (term != null && !term.trim().isEmpty() && year != null && !year.trim().isEmpty()) {
                semester = com.studentmanagement.util.SemesterUtil.formatSemester(term, year);
            }
        }
        if (semester == null || semester.trim().isEmpty()) {
            semester = com.studentmanagement.util.SemesterUtil.getCurrentSemester();
        }
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setPhone(phone);
        student.setDepartment(department);
        student.setSemester(semester.trim().toUpperCase());
        student.setUsername(username);
        student.setPassword(password);

        String errorMessage = userService.registerStudent(student, confirmPassword);

        if (errorMessage == null) {
            request.setAttribute("successMessage", "Account created successfully! Please login.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } else {
            request.setAttribute("errorMessage", errorMessage);
            setSignupEnteredAttributes(request, name, email, phone, department, semester, username);
            request.getRequestDispatcher("/signup.jsp").forward(request, response);
        }
    }

    private void setSignupEnteredAttributes(HttpServletRequest request, String name, String email, String phone, String department, String semester, String username) {
        request.setAttribute("enteredName", name != null ? name : "");
        request.setAttribute("enteredEmail", email != null ? email : "");
        request.setAttribute("enteredPhone", phone != null ? phone : "");
        request.setAttribute("enteredDepartment", department != null ? department : "");
        request.setAttribute("enteredSemester", semester != null ? semester : "");
        request.setAttribute("enteredUsername", username != null ? username : "");
        request.setAttribute("currentSemester", com.studentmanagement.util.SemesterUtil.getCurrentSemester());
        request.setAttribute("availableSemesters", com.studentmanagement.util.SemesterUtil.getAvailableSemesters());
        request.setAttribute("availableYears", com.studentmanagement.util.SemesterUtil.getAvailableYears());
    }
}
