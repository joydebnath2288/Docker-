package com.studentmanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.dao.UserDAO;

public class AuthorizationAndStudentServiceTest {

    private StudentDAO mockStudentDAO;
    private UserDAO mockUserDAO;
    private StudentService studentService;
    private List<String> deletedUsernames;

    private User adminUser;
    private User student1User;
    private User student2User;

    private Student student1Record;
    private Student student2Record;

    @BeforeEach
    public void setUp() {
        deletedUsernames = new ArrayList<>();
        adminUser = new User(1, "admin", "pass", 1, 1);
        student1User = new User(2, "student1", "pass", 2, 1);
        student2User = new User(3, "student2", "pass", 2, 1);

        student1Record = new Student(101, "uid-1", "Student One", "student1@gmail.com", "01711111111", "CSE", 3.80, "student1", "pass");
        student2Record = new Student(102, "uid-2", "Student Two", "student2@gmail.com", "01722222222", "BBA", 3.50, "student2", "pass");

        mockStudentDAO = new StudentDAO() {
            private final List<Student> students = new ArrayList<>(List.of(student1Record, student2Record));

            @Override
            public List<Student> getAllStudents() {
                return new ArrayList<>(students);
            }

            @Override
            public Student getStudentByUid(String uid) {
                for (Student s : students) {
                    if (s.getUid().equals(uid)) {
                        return s;
                    }
                }
                return null;
            }

            @Override
            public boolean addStudent(Student student) {
                students.add(student);
                return true;
            }

            @Override
            public boolean registerStudentWithUser(Student student, User user) {
                students.add(student);
                return true;
            }

            @Override
            public boolean updateStudent(Student student) {
                return true;
            }

            @Override
            public boolean deleteStudent(String uid) {
                return students.removeIf(s -> s.getUid().equals(uid));
            }

            @Override
            public boolean emailExists(String email) {
                return false;
            }

            @Override
            public boolean emailExistsForOther(String email, String uid) {
                return false;
            }
        };

        mockUserDAO = new UserDAO() {
            @Override
            public boolean deleteUser(String username) {
                deletedUsernames.add(username);
                return true;
            }
        };

        studentService = new StudentServiceImpl(mockStudentDAO, mockUserDAO);
    }

    @Test
    public void testScenarioG_StudentAttemptsAdminOnlyOperation() {
        assertFalse(studentService.canManageStudents(student1User));
    }

    @Test
    public void testScenarioH_AdminAccessesPermittedOperations() {
        assertTrue(studentService.canManageStudents(adminUser));
    }

    @Test
    public void testStudentOwnershipVerification() {
        assertTrue(studentService.canViewStudent(student1User, student1Record));
        assertFalse(studentService.canViewStudent(student1User, student2Record));

        assertTrue(studentService.canViewStudent(adminUser, student1Record));
        assertTrue(studentService.canViewStudent(adminUser, student2Record));
    }

    @Test
    public void testGetStudentsForUser() {
        List<Student> adminList = studentService.getStudentsForUser(adminUser);
        assertEquals(2, adminList.size());

        List<Student> studentList = studentService.getStudentsForUser(student1User);
        assertEquals(1, studentList.size());
        assertEquals("Student One", studentList.get(0).getName());
        assertNull(studentList.get(0).getPassword());
    }

    @Test
    public void testScenarioK_StudentCrudAndValidation() {
        Student newStudent = new Student();
        newStudent.setName("Alice Bob");
        newStudent.setEmail("alice@gmail.com");
        newStudent.setPhone("01799999999");
        newStudent.setDepartment("CSE");

        String validationError = studentService.validateStudentForAdd(newStudent, "3.85");
        assertNull(validationError);

        String addResult = studentService.addStudent(newStudent, "3.85");
        assertNull(addResult);

        Student fetched = studentService.getStudentByUid(newStudent.getUid());
        assertNotNull(fetched);
        assertEquals(3.85, fetched.getCgpa());

        String deleteResult = studentService.deleteStudentByUid(newStudent.getUid());
        assertNull(deleteResult);
        assertNull(studentService.getStudentByUid(newStudent.getUid()));
    }

    @Test
    public void testInvalidCgpaValidation() {
        Student student = new Student();
        student.setName("Test Student");
        student.setEmail("test@gmail.com");
        student.setPhone("01788888888");
        student.setDepartment("CSE");

        String errOver4 = studentService.validateStudentField("cgpa", "4.50", null);
        assertEquals("CGPA must be between 0.00 and 4.00.", errOver4);

        String errNonNumeric = studentService.validateStudentField("cgpa", "invalid", null);
        assertEquals("Invalid CGPA format.", errNonNumeric);
    }

    @Test
    public void testDeleteStudentAlsoDeletesUserAccount() {
        Student newStudent = new Student();
        newStudent.setName("Bob Dylan");
        newStudent.setEmail("bob@gmail.com");
        newStudent.setPhone("01711223344");
        newStudent.setDepartment("CSE");
        newStudent.setUsername("bobdylan");
        newStudent.setPassword("pass123");

        String addResult = studentService.addStudent(newStudent, "3.50");
        assertNull(addResult);
        assertNotNull(studentService.getStudentByUid(newStudent.getUid()));

        String delResult = studentService.deleteStudentByUid(newStudent.getUid());
        assertNull(delResult);
        assertNull(studentService.getStudentByUid(newStudent.getUid()));
        assertTrue(deletedUsernames.contains("bobdylan"));
    }
}
