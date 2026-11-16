package com.studentmanagement.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import com.studentmanagement.beans.User;
import com.studentmanagement.config.DBConnection;

public class DatabaseIntegrationTest {

    @Test
    public void testDatabaseConnection() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn);
            assertTrue(conn.isValid(2));
        }
    }

    @Test
    public void testAdminUserInDatabaseHasNumericRoleIdAndStatus() {
        UserDAO userDAO = new UserDAO();
        User admin = userDAO.findByUsername("admin");
        assertNotNull(admin);
        assertEquals("admin", admin.getUsername());
        assertEquals(1, admin.getRoleId());
        assertEquals(1, admin.getStatus());
        assertTrue(admin.isAdmin());
        assertTrue(admin.isActive());
    }

    @Test
    public void testStudentUserInDatabaseHasNumericRoleIdAndStatus() {
        UserDAO userDAO = new UserDAO();
        User student = userDAO.findByUsername("rahim");
        if (student == null) {
            student = userDAO.findByUsername("joydeb");
        }
        if (student == null) {
            student = userDAO.findByUsername("tonima");
        }
        if (student == null) {
            student = userDAO.findByUsername("semuser2026");
        }
        if (student == null) {
            User fallback = new User(0, "student_check_" + System.currentTimeMillis(), "pass123", 2, 1);
            userDAO.createUser(fallback);
            student = userDAO.findByUsername(fallback.getUsername());
        }
        assertNotNull(student);
        assertEquals(2, student.getRoleId());
        assertEquals(1, student.getStatus());
        assertTrue(student.isActive());
    }

    @Test
    public void testAddStudentWithUsernameAndUserCreation() {
        StudentDAO studentDAO = new StudentDAO();
        UserDAO userDAO = new UserDAO();
        com.studentmanagement.service.StudentService service = new com.studentmanagement.service.StudentServiceImpl(studentDAO, userDAO);

        String testEmail = "tahsan_test_" + System.currentTimeMillis() + "@gmail.com";
        String testUsername = "tahsan_u_" + (System.currentTimeMillis() % 1000000);

        com.studentmanagement.beans.Student s = new com.studentmanagement.beans.Student();
        s.setName("Tahsan Khan");
        s.setEmail(testEmail);
        s.setPhone("01236547895");
        s.setDepartment("LAW");
        s.setUsername(testUsername);
        s.setPassword("123456");

        String res = service.addStudent(s, "4.00");
        org.junit.jupiter.api.Assertions.assertNull(res);

        com.studentmanagement.beans.Student fetched = service.getStudentByUid(s.getUid());
        assertNotNull(fetched);
        assertEquals(testUsername, fetched.getUsername());
        User createdUser = userDAO.findByUsername(testUsername);
        assertNotNull(createdUser);
        assertNotNull(fetched.getUserId());
        assertEquals(createdUser.getId(), fetched.getUserId().intValue());

        service.deleteStudentByUid(s.getUid());
        org.junit.jupiter.api.Assertions.assertNull(userDAO.findByUsername(testUsername));
    }

    @Test
    public void testAddStudentWithBlankUsernameAndNoDefaultPassword() {
        StudentDAO studentDAO = new StudentDAO();
        UserDAO userDAO = new UserDAO();
        com.studentmanagement.service.StudentService service = new com.studentmanagement.service.StudentServiceImpl(studentDAO, userDAO);

        String testEmail = "blank_test_" + System.currentTimeMillis() + "@gmail.com";

        com.studentmanagement.beans.Student s = new com.studentmanagement.beans.Student();
        s.setName("No Username Student");
        s.setEmail(testEmail);
        s.setPhone("01799887766");
        s.setDepartment("CSE");
        s.setUsername("   ");
        s.setPassword("   ");

        String res = service.addStudent(s, "3.75");
        org.junit.jupiter.api.Assertions.assertNull(res);

        com.studentmanagement.beans.Student fetched = service.getStudentByUid(s.getUid());
        assertNotNull(fetched);
        assertNotNull(fetched.getUsername());
        assertTrue(fetched.getId() > 0);
        User createdUser = userDAO.findByUsername(fetched.getUsername());
        assertNotNull(createdUser);
        assertEquals(createdUser.getId(), fetched.getId());

        service.deleteStudentByUid(s.getUid());
    }

    @Test
    public void testDeletedStudentCannotLoginAfterDeletion() {
        StudentDAO studentDAO = new StudentDAO();
        UserDAO userDAO = new UserDAO();
        com.studentmanagement.service.StudentService studentService = new com.studentmanagement.service.StudentServiceImpl(studentDAO, userDAO);
        com.studentmanagement.service.UserService userService = new com.studentmanagement.service.UserServiceImpl(userDAO, studentDAO, studentService);

        String testEmail = "login_test_" + System.currentTimeMillis() + "@gmail.com";
        String testUsername = "login_u_" + (System.currentTimeMillis() % 1000000);
        String testPassword = "mypassword123";

        com.studentmanagement.beans.Student s = new com.studentmanagement.beans.Student();
        s.setName("Test Student Login");
        s.setEmail(testEmail);
        s.setPhone("01712399999");
        s.setDepartment("CSE");
        s.setUsername(testUsername);
        s.setPassword(testPassword);

        // 1. Add student
        String res = studentService.addStudent(s, "3.90");
        org.junit.jupiter.api.Assertions.assertNull(res);

        // 2. Verify student can log in
        User loggedIn = userService.authenticate(testUsername, testPassword);
        assertNotNull(loggedIn);
        assertEquals(testUsername, loggedIn.getUsername());

        // 3. Delete student (simulating delete from admin dashboard)
        studentService.deleteStudentByUid(s.getUid());

        // 4. Verify student CANNOT log in anymore!
        User failedLogin = userService.authenticate(testUsername, testPassword);
        org.junit.jupiter.api.Assertions.assertNull(failedLogin);
        assertEquals("Invalid username or password.", userService.getLastAuthError());
    }

    @Test
    public void testStudentTableRelationWithUsersBySingleId() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            // 1. Verify user_id column does NOT exist in students (no two separate IDs!)
            boolean hasUserId = false;
            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM students LIKE 'user_id'")) {
                hasUserId = rs.next();
            }
            assertFalse(hasUserId, "students table should NOT have a separate user_id column (single unified ID)");

            // 2. Verify single id column exists in students
            boolean hasId = false;
            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM students LIKE 'id'")) {
                hasId = rs.next();
            }
            assertTrue(hasId, "students table must have id column");

            // 3. Verify username and password columns do NOT exist in students table
            boolean hasUsername = false;
            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM students LIKE 'username'")) {
                hasUsername = rs.next();
            }
            assertFalse(hasUsername, "students table should NOT have username column (no duplication)");

            boolean hasPassword = false;
            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM students LIKE 'password'")) {
                hasPassword = rs.next();
            }
            assertFalse(hasPassword, "students table should NOT have password column (no duplication)");

            // 4. Verify Foreign Key constraint references users(id) from students(id)
            boolean fkFound = false;
            try (java.sql.ResultSet rs = stmt.executeQuery(
                    "SELECT CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME " +
                    "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'students' AND COLUMN_NAME = 'id'")) {
                while (rs.next()) {
                    if ("users".equalsIgnoreCase(rs.getString("REFERENCED_TABLE_NAME"))
                            && "id".equalsIgnoreCase(rs.getString("REFERENCED_COLUMN_NAME"))) {
                        fkFound = true;
                        break;
                    }
                }
            }
            assertTrue(fkFound, "Foreign key must link students.id to users.id");
        }
    }

    @Test
    public void testStudentResultsRelationWithStudentsByStudentId() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            // 1. Verify student_uid column does NOT exist in student_results
            boolean hasStudentUid = false;
            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM student_results LIKE 'student_uid'")) {
                hasStudentUid = rs.next();
            }
            assertFalse(hasStudentUid, "student_results table should NOT have student_uid column");

            // 2. Verify student_id column exists in student_results
            boolean hasStudentId = false;
            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM student_results LIKE 'student_id'")) {
                hasStudentId = rs.next();
            }
            assertTrue(hasStudentId, "student_results table must have student_id column");

            // 3. Verify Foreign Key constraint references students(id) from student_results(student_id)
            boolean fkFound = false;
            try (java.sql.ResultSet rs = stmt.executeQuery(
                    "SELECT CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME " +
                    "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'student_results' AND COLUMN_NAME = 'student_id'")) {
                while (rs.next()) {
                    if ("students".equalsIgnoreCase(rs.getString("REFERENCED_TABLE_NAME"))
                            && "id".equalsIgnoreCase(rs.getString("REFERENCED_COLUMN_NAME"))) {
                        fkFound = true;
                        break;
                    }
                }
            }
            assertTrue(fkFound, "Foreign key must link student_results.student_id to students.id");
        }
    }

    @Test
    public void testStudentDaoDeleteRemovesBothStudentAndUserRecords() {
        StudentDAO studentDAO = new StudentDAO();
        UserDAO userDAO = new UserDAO();

        String testEmail = "daotest_" + System.currentTimeMillis() + "@gmail.com";
        String testUsername = "daouser_" + (System.currentTimeMillis() % 1000000);

        com.studentmanagement.beans.Student s = new com.studentmanagement.beans.Student();
        s.setName("DAO Deletion Test Student");
        s.setEmail(testEmail);
        s.setPhone("01755443322");
        s.setDepartment("CSE");
        s.setCgpa(3.80);
        s.setUsername(testUsername);
        s.setPassword("secret123");

        boolean added = studentDAO.addStudent(s);
        assertTrue(added, "Student and user should be added: " + studentDAO.getLastError());
        assertNotNull(s.getUid(), "Student UID must be present");
        assertTrue(s.getId() > 0, "Student ID must be set from generated user ID");

        // Verify both records exist
        com.studentmanagement.beans.Student fetchedStudent = studentDAO.getStudentByUid(s.getUid());
        assertNotNull(fetchedStudent, "Student must be queryable by UID");
        User fetchedUser = userDAO.findByUsername(testUsername);
        assertNotNull(fetchedUser, "User must be queryable in users table");

        // Perform deletion using UID directly on StudentDAO
        boolean deleted = studentDAO.deleteStudent(s.getUid());
        assertTrue(deleted, "Student deletion by UID must succeed");

        // Verify BOTH student and user tables have removed the record
        assertNull(studentDAO.getStudentByUid(s.getUid()), "Student must be removed from students table");
        assertNull(userDAO.findByUsername(testUsername), "User must be removed from users table");
        assertNull(userDAO.findById(s.getId()), "User must not be found by ID in users table");
    }
}
