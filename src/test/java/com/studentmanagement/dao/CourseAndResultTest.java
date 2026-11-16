package com.studentmanagement.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.studentmanagement.beans.Course;
import com.studentmanagement.beans.CourseResult;
import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;
import com.studentmanagement.config.DBConnection;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.service.StudentServiceImpl;

public class CourseAndResultTest {

    private static CourseDAO courseDAO;
    private static ResultDAO resultDAO;
    private static StudentDAO studentDAO;
    private static UserDAO userDAO;
    private static StudentService studentService;

    @BeforeAll
    public static void setup() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn);
        }
        courseDAO = new CourseDAO();
        resultDAO = new ResultDAO();
        studentDAO = new StudentDAO();
        userDAO = new UserDAO();
        studentService = new StudentServiceImpl(studentDAO, userDAO, courseDAO, resultDAO);
    }

    @Test
    public void testGradePointAndLetterGradeCalculation() {
        assertEquals(4.00, CourseResult.calculateGradePoint(95.0), 0.001);
        assertEquals("A+", CourseResult.calculateLetterGrade(95.0));

        assertEquals(3.75, CourseResult.calculateGradePoint(88.0), 0.001);
        assertEquals("A", CourseResult.calculateLetterGrade(88.0));

        assertEquals(3.50, CourseResult.calculateGradePoint(81.5), 0.001);
        assertEquals("B+", CourseResult.calculateLetterGrade(81.5));

        assertEquals(3.25, CourseResult.calculateGradePoint(77.0), 0.001);
        assertEquals("B", CourseResult.calculateLetterGrade(77.0));

        assertEquals(3.00, CourseResult.calculateGradePoint(72.0), 0.001);
        assertEquals("C+", CourseResult.calculateLetterGrade(72.0));

        assertEquals(2.75, CourseResult.calculateGradePoint(66.0), 0.001);
        assertEquals("C", CourseResult.calculateLetterGrade(66.0));

        assertEquals(2.50, CourseResult.calculateGradePoint(61.0), 0.001);
        assertEquals("D+", CourseResult.calculateLetterGrade(61.0));

        assertEquals(2.25, CourseResult.calculateGradePoint(50.0), 0.001);
        assertEquals("D", CourseResult.calculateLetterGrade(50.0));

        assertEquals(0.00, CourseResult.calculateGradePoint(45.0), 0.001);
        assertEquals("F", CourseResult.calculateLetterGrade(45.0));
    }

    @Test
    public void testWeightedCgpaFormula() {
        List<CourseResult> list = new ArrayList<>();

        CourseResult r1 = new CourseResult(1, "STU001", 1, 90.0, 4.00, "A+", true);
        r1.setCourseCode("CSE101");
        r1.setCourseName("Programming");
        r1.setCreditHours(3.0);
        list.add(r1);

        CourseResult r2 = new CourseResult(2, "STU001", 2, 85.0, 3.75, "A", true);
        r2.setCourseCode("CSE102");
        r2.setCourseName("Programming Lab");
        r2.setCreditHours(1.0);
        list.add(r2);

        // Sum = (4.00 * 3.0) + (3.75 * 1.0) = 12.0 + 3.75 = 15.75
        // Total credits = 4.0
        // Expected CGPA = 15.75 / 4.0 = 3.9375 -> 3.94
        double cgpa = CourseResult.computeOverallCgpa(list);
        assertEquals(3.94, cgpa, 0.01);
    }

    @Test
    public void testCoursesSeededForAllDepartments() {
        String[] departments = {"CSE", "DATA SCIENCE", "LAW", "PHARMACY", "ENGLISH", "BBA", "CHEMISTRY"};

        for (String dept : departments) {
            List<Course> courses = courseDAO.getCoursesByDepartment(dept);
            assertNotNull(courses, "Course list should not be null for " + dept);
            assertFalse(courses.isEmpty(), "Should have seeded courses for " + dept);
            assertTrue(courses.size() >= 1, "Department " + dept + " must have at least 1 course");
        }
    }

    @Test
    public void testResultDraftVsPublishWorkflow() {
        // Create a test student in CSE
        String testUid = "TEST_RES_" + System.currentTimeMillis();
        String testUser = "res_user_" + (System.currentTimeMillis() % 100000);
        Student testStudent = new Student(0, testUid, "Result Test Student", "test_res_" + System.currentTimeMillis() + "@aiub.edu",
                "01711122233", "CSE", 0.0, "FALL 2025", testUser, "pass123");
        User studentUser = new User(0, testUser, "pass123", 2, 1);

        boolean added = studentDAO.registerStudentWithUser(testStudent, studentUser);
        assertTrue(added, "Test student must be added: " + studentDAO.getLastError());

        try {
            List<Course> cseCourses = courseDAO.getCoursesByDepartment("CSE");
            assertTrue(cseCourses.size() >= 2);

            Course c1 = cseCourses.get(0);
            Course c2 = cseCourses.get(1);

            List<CourseResult> draftResults = new ArrayList<>();
            draftResults.add(new CourseResult(0, testUid, c1.getId(), 92.0, 4.00, "A+", false));
            draftResults.add(new CourseResult(0, testUid, c2.getId(), 86.0, 3.75, "A", false));

            // 1. Admin saves as DRAFT (publish = false)
            User admin = new User(1, "admin", "admin", 1, 1);
            String saveErr = studentService.saveAndPublishResults(admin, testUid, draftResults, false);
            assertEquals(null, saveErr);

            // 2. Student checks their results: should be empty because results are not published!
            List<CourseResult> studentView = studentService.getCourseResultsForStudent(studentUser, testUid);
            assertTrue(studentView.isEmpty(), "Student should NOT see results before they are published");

            // 3. Admin can view draft results
            List<CourseResult> adminView = studentService.getCourseResultsForStudent(admin, testUid);
            assertFalse(adminView.isEmpty(), "Admin should be able to view draft results");

            // 4. Admin publishes the results (publish = true)
            String publishErr = studentService.saveAndPublishResults(admin, testUid, draftResults, true);
            assertEquals(null, publishErr);

            // 5. Now student can view their published results
            List<CourseResult> publishedStudentView = studentService.getCourseResultsForStudent(studentUser, testUid);
            assertFalse(publishedStudentView.isEmpty(), "Student can now view published results");
            assertEquals(2, publishedStudentView.size());

            // 6. Student's CGPA in students table is updated automatically
            Student updatedStudent = studentDAO.getStudentByUid(testUid);
            assertNotNull(updatedStudent);
            assertTrue(updatedStudent.getCgpa() > 0.0, "Student CGPA should be recalculated and updated");

            // 7. Security check: Student cannot publish or edit results
            List<CourseResult> hackedResults = new ArrayList<>();
            hackedResults.add(new CourseResult(0, testUid, c1.getId(), 100.0, 4.00, "A+", true));
            String hackErr = studentService.saveAndPublishResults(studentUser, testUid, hackedResults, true);
            assertNotNull(hackErr, "Student must be forbidden from editing or publishing results");
            assertTrue(hackErr.toLowerCase().contains("permission denied"));

        } finally {
            // Clean up test student and user
            studentDAO.deleteStudent(testUid);
            userDAO.deleteUser(testUser);
        }
    }
}
