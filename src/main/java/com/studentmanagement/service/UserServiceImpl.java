package com.studentmanagement.service;

import java.util.UUID;

import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.util.PasswordUtil;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final StudentDAO studentDAO;
    private final StudentService studentService;
    private String lastAuthError;

    public UserServiceImpl() {
        this(new UserDAO(), new StudentDAO(), new StudentServiceImpl());
    }

    public UserServiceImpl(UserDAO userDAO) {
        this(userDAO, new StudentDAO(), new StudentServiceImpl());
    }

    public UserServiceImpl(UserDAO userDAO, StudentDAO studentDAO) {
        this(userDAO, studentDAO, new StudentServiceImpl(studentDAO, userDAO));
    }

    public UserServiceImpl(UserDAO userDAO, StudentDAO studentDAO, StudentService studentService) {
        this.userDAO = (userDAO != null) ? userDAO : new UserDAO();
        this.studentDAO = (studentDAO != null) ? studentDAO : new StudentDAO();
        this.studentService = (studentService != null) ? studentService : new StudentServiceImpl(this.studentDAO, this.userDAO);
    }

    @Override
    public User authenticate(String username, String password) {
        lastAuthError = null;

        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            lastAuthError = "Username and password are required.";
            return null;
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            lastAuthError = "Invalid username or password.";
            return null;
        }

        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            lastAuthError = "Invalid username or password.";
            return null;
        }

        if (user.getStatus() == 0) {
            lastAuthError = "Account is inactive. Please contact administrator.";
            return null;
        }

        if (user.getStatus() != 1) {
            lastAuthError = "Account status is invalid.";
            return null;
        }

        if (!com.studentmanagement.beans.Role.isValidRoleId(user.getRoleId())) {
            lastAuthError = "Access denied: unsupported user role.";
            return null;
        }

        if (user.isStudent()) {
            Student student = studentDAO.getStudentByUserId(user.getId());
            if (student == null) {
                student = studentDAO.getStudentByUsername(user.getUsername());
            }
            if (student != null) {
                user.setStudent(student);
                student.setUser(user);
            } else {
                // Student record was deleted: remove orphaned user and reject login
                userDAO.deleteUser(user.getUsername());
                lastAuthError = "Invalid username or password.";
                return null;
            }
        }

        return user;
    }

    @Override
    public String getLastAuthError() {
        return lastAuthError;
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        User user = userDAO.findByUsername(username.trim());
        if (user != null && user.isStudent()) {
            Student student = studentDAO.getStudentByUserId(user.getId());
            if (student == null) {
                student = studentDAO.getStudentByUsername(user.getUsername());
            }
            if (student != null) {
                user.setStudent(student);
                student.setUser(user);
            }
        }
        return user;
    }

    @Override
    public boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userDAO.usernameExists(username.trim());
    }

    @Override
    public boolean createUser(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return false;
        }

        String password = user.getPassword();
        if (!password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
            user.setPassword(PasswordUtil.hashPassword(password));
        }

        if (user.getRoleId() <= 0) {
            user.setRoleId(2);
        }

        if (user.getStatus() < 0) {
            user.setStatus(1);
        }

        return userDAO.createUser(user);
    }

    @Override
    public boolean deleteUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userDAO.deleteUser(username.trim());
    }

    @Override
    public String validateUserCredentials(String username, String password, String confirmPassword) {
        if (username == null || username.trim().length() < 6) {
            return "Username must be at least 6 letters.";
        }
        if (password == null || password.length() < 6) {
            return "Password must be at least 6 letters.";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }
        return null;
    }

    @Override
    public String registerStudent(Student student, String confirmPassword) {
        if (student == null
                || student.getName() == null || student.getName().trim().isEmpty()
                || student.getEmail() == null || student.getEmail().trim().isEmpty()
                || student.getPhone() == null || student.getPhone().trim().isEmpty()
                || student.getDepartment() == null || student.getDepartment().trim().isEmpty()
                || student.getUsername() == null || student.getUsername().trim().isEmpty()
                || student.getPassword() == null || student.getPassword().isEmpty()
                || confirmPassword == null || confirmPassword.isEmpty()) {
            return "All fields are required.";
        }

        student.setName(student.getName().trim());
        student.setEmail(student.getEmail().trim());
        student.setPhone(student.getPhone().trim());
        student.setDepartment(student.getDepartment().trim());
        student.setUsername(student.getUsername().trim());

        String studentValidationError = studentService.validateStudentSignup(
                student.getName(), student.getEmail(), student.getPhone(), student.getDepartment());
        if (studentValidationError != null) {
            return studentValidationError;
        }

        String userValidationError = validateUserCredentials(
                student.getUsername(), student.getPassword(), confirmPassword);
        if (userValidationError != null) {
            return userValidationError;
        }

        if (studentDAO.emailExists(student.getEmail())) {
            return "Email already exists. Same email will not be accepted.";
        }

        if (usernameExists(student.getUsername())) {
            return "Username already exists. Same username will not be accepted.";
        }

        User newUser = new User();
        newUser.setUsername(student.getUsername());
        newUser.setPassword(PasswordUtil.hashPassword(student.getPassword()));
        newUser.setRoleId(2);
        newUser.setStatus(1);

        student.setUser(newUser);
        newUser.setStudent(student);

        if (student.getUid() == null || student.getUid().trim().isEmpty()) {
            student.setUid(UUID.randomUUID().toString());
        }
        student.setCgpa(0.00);

        boolean registered = studentDAO.registerStudentWithUser(student, newUser);
        if (!registered) {
            String err = studentDAO.getLastError();
            return err != null ? err : "Failed to create account. Please try again.";
        }

        return null;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.isAdmin();
    }

    @Override
    public boolean isStudent(User user) {
        return user != null && user.isStudent();
    }

    @Override
    public boolean isStaff(User user) {
        return user != null && user.isStaff();
    }

    @Override
    public boolean isActive(User user) {
        return user != null && user.isActive();
    }

    @Override
    public String getDashboardRoute(User user) {
        if (user == null) {
            return "/login";
        }
        if (user.isStaff()) {
            return "/admin/dashboard";
        }
        if (user.isStudent()) {
            return "/student/dashboard";
        }
        return "/login";
    }
}
