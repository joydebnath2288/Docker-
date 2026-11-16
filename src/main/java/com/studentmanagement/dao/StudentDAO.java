package com.studentmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;
import com.studentmanagement.config.DBConnection;

public class StudentDAO {

    private static final String SELECT_STUDENT_BASE = 
            "SELECT s.id, s.uid, s.name, s.email, s.phone, s.department, s.cgpa, s.semester, "
            + "u.username, u.password, u.role, u.status "
            + "FROM students s "
            + "LEFT JOIN users u ON s.id = u.id";

    private static final String SELECT_ALL_STUDENTS = SELECT_STUDENT_BASE + " ORDER BY s.id ASC";
    private static final String SELECT_STUDENT_BY_ID = SELECT_STUDENT_BASE + " WHERE s.id = ?";
    private static final String SELECT_STUDENT_BY_UID = SELECT_STUDENT_BASE + " WHERE s.uid = ?";
    private static final String SELECT_STUDENT_BY_USERNAME = SELECT_STUDENT_BASE + " WHERE u.username = ? OR s.email = ?";
    private static final String INSERT_STUDENT = "INSERT INTO students (id, uid, name, email, phone, department, cgpa, semester) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_STUDENT_BY_UID = "UPDATE students SET name = ?, email = ?, phone = ?, department = ?, cgpa = ?, semester = ? WHERE uid = ?";
    private static final String DELETE_STUDENT_BY_UID = "DELETE FROM students WHERE uid = ?";

    private String lastError = null;

    public String getLastError() {
        return lastError;
    }

    private void handleException(String method, SQLException e) {
        System.err.println("Error in " + method + ": " + e.getMessage());
        e.printStackTrace();

        String msg = e.getMessage();
        if (msg != null && (msg.contains("Connection refused") || msg.contains("Communications link failure"))) {
            lastError = "Cannot connect to MySQL Server at localhost:3306. Please ensure MySQL service is installed and running.";
        } else if (msg != null && msg.contains("Access denied")) {
            lastError = "MySQL Access Denied! Please check the password in DBConnection.java.";
        } else if (msg != null && msg.contains("Unknown database")) {
            lastError = "Database 'student_management' not found! Please execute database.sql in MySQL.";
        } else {
            lastError = msg;
        }
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        lastError = null;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(SELECT_ALL_STUDENTS);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                students.add(mapStudent(rs));
            }

        } catch (SQLException e) {
            handleException("getAllStudents", e);
        }

        return students;
    }

    public Student getStudentByUid(String uid) {
        lastError = null;
        if (uid == null || uid.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(SELECT_STUDENT_BY_UID)) {

            ps.setString(1, uid.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }

        } catch (SQLException e) {
            handleException("getStudentByUid", e);
        }

        return null;
    }

    public Student getStudentById(int id) {
        if (id <= 0) {
            return null;
        }
        lastError = null;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(SELECT_STUDENT_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }

        } catch (SQLException e) {
            handleException("getStudentById", e);
        }

        return null;
    }

    public Student getStudentByUserId(int userId) {
        return getStudentById(userId);
    }

    public Student getStudentByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        lastError = null;

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(SELECT_STUDENT_BY_USERNAME)) {

            ps.setString(1, username.trim());
            ps.setString(2, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }

        } catch (SQLException e) {
            handleException("getStudentByUsername", e);
        }

        return null;
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        int id = rs.getInt("id");
        student.setId(id);
        student.setUid(rs.getString("uid"));
        student.setName(rs.getString("name"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        student.setDepartment(rs.getString("department"));
        student.setCgpa(rs.getDouble("cgpa"));
        String sem = rs.getString("semester");
        student.setSemester((sem != null && !sem.trim().isEmpty()) ? sem.trim() : com.studentmanagement.util.SemesterUtil.getCurrentSemester());

        String username = rs.getString("username");
        String password = rs.getString("password");
        if ((id > 0) || (username != null && !username.trim().isEmpty())) {
            int role = rs.getInt("role");
            int status = rs.getInt("status");
            User user = new User(id, username != null ? username.trim() : null, password, role > 0 ? role : 2, status >= 0 ? status : 1);
            student.setUser(user);
            user.setStudent(student);
        }
        return student;
    }

    private void setStudentParameters(PreparedStatement ps, Student student) throws SQLException {
        ps.setInt(1, student.getId());
        ps.setString(2, student.getUid().trim());
        ps.setString(3, student.getName());
        ps.setString(4, student.getEmail());
        ps.setString(5, student.getPhone());
        ps.setString(6, student.getDepartment());
        ps.setDouble(7, student.getCgpa());
        String sem = cleanString(student.getSemester());
        ps.setString(8, sem != null ? sem : com.studentmanagement.util.SemesterUtil.getCurrentSemester());
    }

    private String cleanString(String s) {
        return (s != null && !s.trim().isEmpty()) ? s.trim() : null;
    }

    public boolean addStudent(Student student) {
        lastError = null;
        if (student == null) {
            lastError = "Student object is required.";
            return false;
        }

        if (student.getUid() == null || student.getUid().trim().isEmpty()) {
            student.setUid(java.util.UUID.randomUUID().toString());
        }

        User user = student.getUser();
        if (user == null && student.getUsername() != null && !student.getUsername().trim().isEmpty()) {
            String pass = student.getPassword();
            if (pass == null || pass.trim().isEmpty()) {
                pass = com.studentmanagement.util.PasswordUtil.hashPassword(java.util.UUID.randomUUID().toString().substring(0, 8));
            }
            user = new User(0, student.getUsername().trim(), pass, 2, 1);
            student.setUser(user);
            user.setStudent(student);
        }

        String insertUserSql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId = student.getId();

                if (user != null && userId <= 0) {
                    try (PreparedStatement psUser = conn.prepareStatement(insertUserSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                        psUser.setString(1, user.getUsername().trim());
                        psUser.setString(2, user.getPassword());
                        psUser.setInt(3, user.getRoleId() > 0 ? user.getRoleId() : 2);
                        psUser.setInt(4, user.getStatus() >= 0 ? user.getStatus() : 1);
                        int uResult = psUser.executeUpdate();
                        if (uResult > 0) {
                            try (ResultSet keys = psUser.getGeneratedKeys()) {
                                if (keys.next()) {
                                    userId = keys.getInt(1);
                                    user.setId(userId);
                                    student.setId(userId);
                                }
                            }
                        }
                    }
                }

                if (userId <= 0) {
                    conn.rollback();
                    lastError = "User account could not be created or resolved for student.";
                    return false;
                }

                try (PreparedStatement psStudent = conn.prepareStatement(INSERT_STUDENT)) {
                    setStudentParameters(psStudent, student);
                    int sResult = psStudent.executeUpdate();

                    if (sResult > 0) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        lastError = "Failed to insert student record.";
                        return false;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                handleException("addStudent", e);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            handleException("addStudent", e);
            return false;
        }
    }

    public boolean registerStudentWithUser(Student student, User user) {
        if (student == null) {
            lastError = "Student object is required.";
            return false;
        }
        if (user != null) {
            student.setUser(user);
            user.setStudent(student);
        }
        return addStudent(student);
    }

    public boolean updateStudent(Student student) {
        lastError = null;
        if (student == null || student.getUid() == null || student.getUid().trim().isEmpty()) {
            lastError = "Student object and UID are required.";
            return false;
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(UPDATE_STUDENT_BY_UID)) {

            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPhone());
            ps.setString(4, student.getDepartment());
            ps.setDouble(5, student.getCgpa());
            String sem = cleanString(student.getSemester());
            ps.setString(6, sem != null ? sem : com.studentmanagement.util.SemesterUtil.getCurrentSemester());
            ps.setString(7, student.getUid().trim());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            handleException("updateStudent", e);
        }

        return false;
    }

    public boolean updateStudentSemester(String uid, String semester) {
        lastError = null;
        if (uid == null || uid.trim().isEmpty() || semester == null || semester.trim().isEmpty()) {
            lastError = "Student UID and semester are required.";
            return false;
        }

        String sql = "UPDATE students SET semester = ? WHERE uid = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semester.trim());
            ps.setString(2, uid.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("updateStudentSemester", e);
            return false;
        }
    }

    public boolean deleteStudent(String uid) {
        boolean rowDeleted = false;
        lastError = null;

        if (uid == null || uid.trim().isEmpty()) {
            lastError = "Student UID is required for deletion.";
            return false;
        }

        String trimmedUid = uid.trim();
        String selectIdSql = "SELECT id FROM students WHERE uid = ?";
        String deleteUserSql = "DELETE FROM users WHERE id = ?";
        String deleteStudentSql = DELETE_STUDENT_BY_UID;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId = -1;
                try (PreparedStatement psSelect = conn.prepareStatement(selectIdSql)) {
                    psSelect.setString(1, trimmedUid);
                    try (ResultSet rs = psSelect.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("id");
                        }
                    }
                }

                if (userId <= 0) {
                    conn.rollback();
                    lastError = "Student not found with UID: " + trimmedUid;
                    return false;
                }

                // 1. Delete from users table:
                // Because students(id) REFERENCES users(id) ON DELETE CASCADE,
                // this also automatically cascades to delete the student in students table
                // and any records in student_results referencing students(uid).
                try (PreparedStatement psUser = conn.prepareStatement(deleteUserSql)) {
                    psUser.setInt(1, userId);
                    int affectedUser = psUser.executeUpdate();
                    if (affectedUser > 0) {
                        rowDeleted = true;
                    }
                }

                // 2. Safety check: ensure deleted from students if cascade was not configured
                try (PreparedStatement psStudent = conn.prepareStatement(deleteStudentSql)) {
                    psStudent.setString(1, trimmedUid);
                    int affectedStudent = psStudent.executeUpdate();
                    if (affectedStudent > 0) {
                        rowDeleted = true;
                    }
                }

                if (rowDeleted) {
                    conn.commit();
                } else {
                    conn.rollback();
                    lastError = "Failed to delete student and associated user.";
                }
            } catch (SQLException e) {
                conn.rollback();
                handleException("deleteStudent", e);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            handleException("deleteStudent", e);
            return false;
        }

        return rowDeleted;
    }

    public boolean deleteStudentById(int id) {
        if (id <= 0) {
            lastError = "Student ID is required for deletion.";
            return false;
        }
        Student student = getStudentById(id);
        if (student != null && student.getUid() != null) {
            return deleteStudent(student.getUid());
        }
        lastError = "Student not found with ID: " + id;
        return false;
    }

    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM students WHERE LOWER(TRIM(email)) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            handleException("emailExists", e);
        }

        return false;
    }

    public boolean emailExistsForOther(String email, String uid) {
        if (email == null || email.trim().isEmpty() || uid == null || uid.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM students WHERE LOWER(TRIM(email)) = LOWER(?) AND uid != ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setString(2, uid.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            handleException("emailExistsForOther", e);
        }

        return false;
    }
}
