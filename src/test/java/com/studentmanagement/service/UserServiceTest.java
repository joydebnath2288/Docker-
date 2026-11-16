package com.studentmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.util.PasswordUtil;

public class UserServiceTest {

    private UserDAO mockUserDAO;
    private StudentDAO mockStudentDAO;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        mockUserDAO = new UserDAO() {
            @Override
            public User findByUsername(String username) {
                if ("admin".equals(username)) {
                    return new User(1, "admin", PasswordUtil.hashPassword("admin123"), 1, 1);
                } else if ("student1".equals(username)) {
                    return new User(2, "student1", PasswordUtil.hashPassword("student123"), 2, 1);
                } else if ("inactiveUser".equals(username)) {
                    return new User(3, "inactiveUser", PasswordUtil.hashPassword("secret123"), 2, 0);
                } else if ("unknownRole".equals(username)) {
                    return new User(4, "unknownRole", PasswordUtil.hashPassword("secret123"), 99, 1);
                }
                return null;
            }

            @Override
            public boolean usernameExists(String username) {
                return "admin".equals(username) || "student1".equals(username);
            }

            @Override
            public boolean createUser(User user) {
                return true;
            }
        };

        mockStudentDAO = new StudentDAO() {
            @Override
            public boolean registerStudentWithUser(Student student, User user) {
                assertEquals(2, user.getRoleId());
                assertEquals(1, user.getStatus());
                return true;
            }

            @Override
            public boolean emailExists(String email) {
                return false;
            }

            @Override
            public Student getStudentByUsername(String username) {
                if ("student1".equals(username)) {
                    Student s = new Student();
                    s.setUsername("student1");
                    return s;
                }
                return null;
            }
        };

        userService = new UserServiceImpl(mockUserDAO, mockStudentDAO);
    }

    @Test
    public void testScenarioA_ValidAdminLogin() {
        User user = userService.authenticate("admin", "admin123");
        assertNotNull(user);
        assertEquals(1, user.getRoleId());
        assertEquals(1, user.getStatus());
        assertTrue(userService.isAdmin(user));
        assertEquals("/admin/dashboard", userService.getDashboardRoute(user));
    }

    @Test
    public void testScenarioB_ValidStudentLogin() {
        User user = userService.authenticate("student1", "student123");
        assertNotNull(user);
        assertEquals(2, user.getRoleId());
        assertEquals(1, user.getStatus());
        assertTrue(userService.isStudent(user));
        assertFalse(userService.isAdmin(user));
        assertEquals("/student/dashboard", userService.getDashboardRoute(user));
    }

    @Test
    public void testScenarioC_IncorrectPassword() {
        User user = userService.authenticate("admin", "wrongPassword");
        assertNull(user);
        assertEquals("Invalid username or password.", userService.getLastAuthError());
    }

    @Test
    public void testScenarioD_UnknownUsername() {
        User user = userService.authenticate("nonExistentUser", "somePass123");
        assertNull(user);
        assertEquals("Invalid username or password.", userService.getLastAuthError());
    }

    @Test
    public void testScenarioE_InactiveAccountDenied() {
        User user = userService.authenticate("inactiveUser", "secret123");
        assertNull(user);
        assertEquals("Account is inactive. Please contact administrator.", userService.getLastAuthError());
    }

    @Test
    public void testScenarioF_UnsupportedRoleIdDenied() {
        User user = userService.authenticate("unknownRole", "secret123");
        assertNull(user);
        assertEquals("Access denied: unsupported user role.", userService.getLastAuthError());
    }

    @Test
    public void testScenarioI_ExistingSignupCreatesStudentRoleAndStatus() {
        Student student = new Student();
        student.setName("New Student");
        student.setEmail("newstudent@gmail.com");
        student.setPhone("01712345678");
        student.setDepartment("CSE");
        student.setUsername("newstudent");
        student.setPassword("studentPass123");

        String error = userService.registerStudent(student, "studentPass123");
        assertNull(error);
    }

    @Test
    public void testScenarioL_ExistingAccountPasswordHashesAuthenticateThroughPasswordUtil() {
        String originalPassword = "mySecretPassword";
        String hashedPassword = PasswordUtil.hashPassword(originalPassword);

        assertTrue(PasswordUtil.checkPassword(originalPassword, hashedPassword));
        assertFalse(PasswordUtil.checkPassword("wrongPassword", hashedPassword));
    }

    @Test
    public void testScenarioM_DeletedStudentAccountCannotLogin() {
        // Mock user exists in users table, but studentDAO has no corresponding record
        User user = userService.authenticate("inactiveUser", "secret123");
        // inactiveUser is inactive, but let's test a user whose student record is null
        // Create an orphaned student user
        User orphaned = new User(10, "deletedStudent", PasswordUtil.hashPassword("pass123"), 2, 1);
        UserDAO daoWithOrphan = new UserDAO() {
            private boolean userDeleted = false;
            @Override
            public User findByUsername(String username) {
                if ("deletedStudent".equals(username) && !userDeleted) return orphaned;
                return null;
            }
            @Override
            public boolean deleteUser(String username) {
                if ("deletedStudent".equals(username)) {
                    userDeleted = true;
                    return true;
                }
                return false;
            }
        };
        UserService orphanService = new UserServiceImpl(daoWithOrphan, mockStudentDAO);
        User authResult = orphanService.authenticate("deletedStudent", "pass123");
        assertNull(authResult);
        assertEquals("Invalid username or password.", orphanService.getLastAuthError());
    }
}
