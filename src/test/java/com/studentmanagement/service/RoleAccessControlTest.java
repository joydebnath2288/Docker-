
package com.studentmanagement.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.studentmanagement.beans.Role;
import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.util.PasswordUtil;


public class RoleAccessControlTest {

    private UserService userService;
    private StudentService studentService;

    private User adminUser;
    private User facultyUser;
    private User registrarUser;
    private User accountsUser;
    private User supervisorUser;
    private User studentUser;

    private Student studentRecord1;
    private Student studentRecord2;

    @BeforeEach
    public void setUp() {
        adminUser = new User(1, "admin", PasswordUtil.hashPassword("admin123"), Role.ADMIN.getId(), 1);
        studentUser = new User(2, "student1", PasswordUtil.hashPassword("student123"), Role.STUDENT.getId(), 1);
        facultyUser = new User(3, "faculty", PasswordUtil.hashPassword("faculty123"), Role.FACULTY.getId(), 1);
        registrarUser = new User(4, "registrar", PasswordUtil.hashPassword("registrar123"), Role.REGISTRAR.getId(), 1);
        accountsUser = new User(5, "accounts", PasswordUtil.hashPassword("accounts123"), Role.ACCOUNTS.getId(), 1);
        supervisorUser = new User(6, "supervisor", PasswordUtil.hashPassword("supervisor123"), Role.SUPERVISOR.getId(), 1);

        studentRecord1 = new Student(101, "uid-101", "Alice Wonder", "alice@gmail.com", "01711111111", "CSE", 3.85, "student1", "student123");
        studentRecord2 = new Student(102, "uid-102", "Bob Marley", "bob@gmail.com", "01722222222", "BBA", 3.40, "student2", "student123");

        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public User findByUsername(String username) {
                if ("admin".equalsIgnoreCase(username)) return adminUser;
                if ("student1".equalsIgnoreCase(username)) return studentUser;
                if ("faculty".equalsIgnoreCase(username)) return facultyUser;
                if ("registrar".equalsIgnoreCase(username)) return registrarUser;
                if ("accounts".equalsIgnoreCase(username)) return accountsUser;
                if ("supervisor".equalsIgnoreCase(username)) return supervisorUser;
                if ("unknown".equalsIgnoreCase(username)) {
                    return new User(99, "unknown", PasswordUtil.hashPassword("pass123"), 99, 1);
                }
                return null;
            }

            @Override
            public boolean usernameExists(String username) {
                return findByUsername(username) != null;
            }
        };

        StudentDAO mockStudentDAO = new StudentDAO() {
            @Override
            public List<Student> getAllStudents() {
                return List.of(studentRecord1, studentRecord2);
            }

            @Override
            public Student getStudentByUsername(String username) {
                if ("student1".equalsIgnoreCase(username)) return studentRecord1;
                return null;
            }

            @Override
            public Student getStudentByUid(String uid) {
                if ("uid-101".equalsIgnoreCase(uid)) return studentRecord1;
                if ("uid-102".equalsIgnoreCase(uid)) return studentRecord2;
                return null;
            }
        };

        userService = new UserServiceImpl(mockUserDAO, mockStudentDAO);
        studentService = new StudentServiceImpl(mockStudentDAO, mockUserDAO, null, null);
    }

    @Test
    public void testRoleEnumDefinitionsAndPermissions() {
        Role admin = Role.ADMIN;
        assertTrue(admin.isAdmin());
        assertTrue(admin.isStaff());
        assertFalse(admin.isStudent());
        assertTrue(admin.canViewDirectory());
        assertTrue(admin.canAddStudent());
        assertTrue(admin.canEditStudent());
        assertTrue(admin.canDeleteStudent());
        assertTrue(admin.canAssignCourses());
        assertTrue(admin.canGradeResults());
        assertTrue(admin.canPublishResults());

        Role faculty = Role.FACULTY;
        assertFalse(faculty.isAdmin());
        assertTrue(faculty.isStaff());
        assertFalse(faculty.isStudent());
        assertTrue(faculty.canViewDirectory());
        assertFalse(faculty.canAddStudent());
        assertFalse(faculty.canEditStudent());
        assertFalse(faculty.canDeleteStudent());
        assertTrue(faculty.canAssignCourses());
        assertTrue(faculty.canGradeResults());
        assertFalse(faculty.canPublishResults());

        // REGISTRAR: student registration, course assignment, editing student profile, no grading or deletion
        Role registrar = Role.REGISTRAR;
        assertFalse(registrar.isAdmin());
        assertTrue(registrar.isStaff());
        assertFalse(registrar.isStudent());
        assertTrue(registrar.canViewDirectory());
        assertTrue(registrar.canAddStudent());
        assertTrue(registrar.canEditStudent());
        assertFalse(registrar.canDeleteStudent());
        assertTrue(registrar.canAssignCourses());
        assertFalse(registrar.canGradeResults());
        assertFalse(registrar.canPublishResults());

        // ACCOUNTS: directory inspection, fee/registration overview, read-only
        Role accounts = Role.ACCOUNTS;
        assertFalse(accounts.isAdmin());
        assertTrue(accounts.isStaff());
        assertFalse(accounts.isStudent());
        assertTrue(accounts.canViewDirectory());
        assertFalse(accounts.canAddStudent());
        assertFalse(accounts.canEditStudent());
        assertFalse(accounts.canDeleteStudent());
        assertFalse(accounts.canAssignCourses());
        assertFalse(accounts.canGradeResults());
        assertFalse(accounts.canPublishResults());

        // SUPERVISOR: directory oversight, analytics, result publishing, no student deletion or info edits
        Role supervisor = Role.SUPERVISOR;
        assertFalse(supervisor.isAdmin());
        assertTrue(supervisor.isStaff());
        assertFalse(supervisor.isStudent());
        assertTrue(supervisor.canViewDirectory());
        assertFalse(supervisor.canAddStudent());
        assertFalse(supervisor.canEditStudent());
        assertFalse(supervisor.canDeleteStudent());
        assertFalse(supervisor.canAssignCourses());
        assertFalse(supervisor.canGradeResults());
        assertTrue(supervisor.canPublishResults());

        // STUDENT: student self-service only
        Role student = Role.STUDENT;
        assertFalse(student.isAdmin());
        assertFalse(student.isStaff());
        assertTrue(student.isStudent());
        assertFalse(student.canViewDirectory());
        assertFalse(student.canAddStudent());
        assertFalse(student.canEditStudent());
        assertFalse(student.canDeleteStudent());
        assertFalse(student.canAssignCourses());
        assertFalse(student.canGradeResults());
        assertFalse(student.canPublishResults());
    }

    @Test
    public void testRoleFromIdAndCode() {
        assertEquals(Role.ADMIN, Role.fromId(1));
        assertEquals(Role.STUDENT, Role.fromId(2));
        assertEquals(Role.FACULTY, Role.fromId(3));
        assertEquals(Role.REGISTRAR, Role.fromId(4));
        assertEquals(Role.ACCOUNTS, Role.fromId(5));
        assertEquals(Role.SUPERVISOR, Role.fromId(6));
        assertNull(Role.fromId(99));

        assertEquals(Role.FACULTY, Role.fromCode("FACULTY"));
        assertEquals(Role.FACULTY, Role.fromCode("faculty"));
        assertEquals(Role.SUPERVISOR, Role.fromCode("supervisor"));
        assertEquals(Role.REGISTRAR, Role.fromCode("register"));
        assertEquals(Role.ACCOUNTS, Role.fromCode("acounts"));
        assertNull(Role.fromCode("nonexistent"));

        assertTrue(Role.isValidRoleId(1));
        assertTrue(Role.isValidRoleId(3));
        assertTrue(Role.isValidRoleId(4));
        assertTrue(Role.isValidRoleId(5));
        assertTrue(Role.isValidRoleId(6));
        assertFalse(Role.isValidRoleId(99));
        assertFalse(Role.isValidRoleId(0));
    }

    @Test
    public void testUserBeanRoleDelegations() {
        assertEquals("Faculty", facultyUser.getRoleName());
        assertEquals("FACULTY", facultyUser.getRoleCode());
        assertEquals("bg-info text-dark", facultyUser.getBadgeClass());
        assertTrue(facultyUser.isCanGradeResults());
        assertTrue(facultyUser.isCanAssignCourses());
        assertFalse(facultyUser.isCanDeleteStudent());

        assertEquals("Supervisor", supervisorUser.getRoleName());
        assertEquals("SUPERVISOR", supervisorUser.getRoleCode());
        assertEquals("bg-dark", supervisorUser.getBadgeClass());
        assertTrue(supervisorUser.isCanPublishResults());
        assertFalse(supervisorUser.isCanDeleteStudent());

        assertEquals("Registrar", registrarUser.getRoleName());
        assertEquals("REGISTRAR", registrarUser.getRoleCode());
        assertEquals("bg-success", registrarUser.getBadgeClass());
        assertTrue(registrarUser.isCanAddStudent());
        assertTrue(registrarUser.isCanEditStudent());
        assertFalse(registrarUser.isCanGradeResults());

        assertEquals("Accounts", accountsUser.getRoleName());
        assertEquals("ACCOUNTS", accountsUser.getRoleCode());
        assertEquals("bg-warning text-dark", accountsUser.getBadgeClass());
        assertTrue(accountsUser.isCanViewDirectory());
        assertFalse(accountsUser.isCanAddStudent());
    }

    @Test
    public void testAuthenticationAndRoutingForMultipleRoles() {
        // Faculty Login
        User fAuth = userService.authenticate("faculty", "faculty123");
        assertNotNull(fAuth);
        assertEquals(Role.FACULTY.getId(), fAuth.getRoleId());
        assertTrue(fAuth.isStaff());
        assertFalse(fAuth.isAdmin());
        assertEquals("/admin/dashboard", userService.getDashboardRoute(fAuth));

        // Registrar Login
        User rAuth = userService.authenticate("registrar", "registrar123");
        assertNotNull(rAuth);
        assertEquals(Role.REGISTRAR.getId(), rAuth.getRoleId());
        assertTrue(rAuth.isStaff());
        assertEquals("/admin/dashboard", userService.getDashboardRoute(rAuth));

        // Accounts Login
        User aAuth = userService.authenticate("accounts", "accounts123");
        assertNotNull(aAuth);
        assertEquals(Role.ACCOUNTS.getId(), aAuth.getRoleId());
        assertTrue(aAuth.isStaff());
        assertEquals("/admin/dashboard", userService.getDashboardRoute(aAuth));

        // Supervisor Login
        User sAuth = userService.authenticate("supervisor", "supervisor123");
        assertNotNull(sAuth);
        assertEquals(Role.SUPERVISOR.getId(), sAuth.getRoleId());
        assertTrue(sAuth.isStaff());
        assertEquals("/admin/dashboard", userService.getDashboardRoute(sAuth));

        // Student Login
        User stAuth = userService.authenticate("student1", "student123");
        assertNotNull(stAuth);
        assertEquals(Role.STUDENT.getId(), stAuth.getRoleId());
        assertFalse(stAuth.isStaff());
        assertTrue(stAuth.isStudent());
        assertEquals("/student/dashboard", userService.getDashboardRoute(stAuth));

        // Unknown role rejected
        User unknown = userService.authenticate("unknown", "pass123");
        assertNull(unknown);
    }

    @Test
    public void testStudentDirectoryAccessPermissions() {
        // All staff roles can view directory
        assertTrue(studentService.canManageStudents(adminUser));
        assertTrue(studentService.canManageStudents(facultyUser));
        assertTrue(studentService.canManageStudents(registrarUser));
        assertTrue(studentService.canManageStudents(accountsUser));
        assertTrue(studentService.canManageStudents(supervisorUser));
        assertFalse(studentService.canManageStudents(studentUser));

        // Staff can view any student
        assertTrue(studentService.canViewStudent(facultyUser, studentRecord1));
        assertTrue(studentService.canViewStudent(facultyUser, studentRecord2));
        assertTrue(studentService.canViewStudent(supervisorUser, studentRecord1));

        // Student can only view own record
        assertTrue(studentService.canViewStudent(studentUser, studentRecord1));
        assertFalse(studentService.canViewStudent(studentUser, studentRecord2));

        // getStudentsForUser returns all students for staff, only own record for student
        assertEquals(2, studentService.getStudentsForUser(facultyUser).size());
        assertEquals(2, studentService.getStudentsForUser(registrarUser).size());
        assertEquals(2, studentService.getStudentsForUser(accountsUser).size());
        assertEquals(2, studentService.getStudentsForUser(supervisorUser).size());
        assertEquals(1, studentService.getStudentsForUser(studentUser).size());
    }
}
