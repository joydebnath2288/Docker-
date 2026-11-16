package com.studentmanagement.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.studentmanagement.beans.CourseResult;
import com.studentmanagement.config.DBConnection;

public class ResultDAO {

    private static final String SELECT_RESULTS_WITH_ALL_DEPT_COURSES =
            "SELECT c.id AS course_id, c.course_code, c.course_name, c.department, c.credit_hours, " +
            "       r.id AS result_id, r.student_id, r.marks, r.grade_point, r.letter_grade, r.published, " +
            "       r.semester, r.enrolled_by, r.evaluated " +
            "FROM courses c " +
            "LEFT JOIN student_results r ON c.id = r.course_id AND r.student_id = ? " +
            "WHERE UPPER(c.department) = UPPER(?) " +
            "ORDER BY (CASE WHEN r.id IS NOT NULL AND r.evaluated = TRUE THEN 0 WHEN r.id IS NOT NULL THEN 1 ELSE 2 END) ASC, c.course_code ASC";

    private static final String SELECT_STUDENT_RESULTS =
            "SELECT r.id AS result_id, r.student_id, r.course_id, r.marks, r.grade_point, r.letter_grade, r.published, " +
            "       r.semester, r.enrolled_by, r.evaluated, " +
            "       c.course_code, c.course_name, c.department, c.credit_hours " +
            "FROM student_results r " +
            "INNER JOIN courses c ON r.course_id = c.id " +
            "WHERE r.student_id = ? " +
            "ORDER BY r.semester DESC, c.course_code ASC";

    private static final String SELECT_PUBLISHED_STUDENT_RESULTS =
            "SELECT r.id AS result_id, r.student_id, r.course_id, r.marks, r.grade_point, r.letter_grade, r.published, " +
            "       r.semester, r.enrolled_by, r.evaluated, " +
            "       c.course_code, c.course_name, c.department, c.credit_hours " +
            "FROM student_results r " +
            "INNER JOIN courses c ON r.course_id = c.id " +
            "WHERE r.student_id = ? AND r.published = TRUE AND r.evaluated = TRUE " +
            "ORDER BY r.semester DESC, c.course_code ASC";

    private static final String UPSERT_RESULT =
            "INSERT INTO student_results (student_id, course_id, semester, marks, grade_point, letter_grade, published, evaluated, enrolled_by) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE semester = VALUES(semester), marks = VALUES(marks), " +
            "                       grade_point = VALUES(grade_point), letter_grade = VALUES(letter_grade), " +
            "                       published = VALUES(published), evaluated = VALUES(evaluated), " +
            "                       enrolled_by = VALUES(enrolled_by)";

    private static final String UPDATE_STUDENT_CGPA =
            "UPDATE students SET cgpa = ? WHERE id = ?";

    public int resolveStudentId(String studentUid) {
        if (studentUid == null || studentUid.trim().isEmpty()) return -1;
        try {
            return Integer.parseInt(studentUid.trim());
        } catch (NumberFormatException ignored) {}

        String sql = "SELECT id FROM students WHERE uid = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentUid.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error resolving studentId from UID: " + e.getMessage());
        }
        return -1;
    }

    public List<CourseResult> getCourseResultsForDepartment(int studentId, String department) {
        List<CourseResult> list = new ArrayList<>();
        if (department == null) return list;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_RESULTS_WITH_ALL_DEPT_COURSES)) {
            ps.setInt(1, studentId);
            ps.setString(2, department.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CourseResult res = new CourseResult();
                    res.setId(rs.getInt("result_id"));
                    res.setStudentId(studentId);
                    res.setCourseId(rs.getInt("course_id"));
                    res.setCourseCode(rs.getString("course_code"));
                    res.setCourseName(rs.getString("course_name"));
                    res.setDepartment(rs.getString("department"));
                    res.setCreditHours(rs.getDouble("credit_hours"));

                    Object marksObj = rs.getObject("marks");
                    boolean hasMarks = (marksObj != null);
                    double marks = hasMarks ? rs.getDouble("marks") : -1.0;
                    res.setMarks(hasMarks ? marks : 0.0);

                    double gp = rs.getDouble("grade_point");
                    String lg = rs.getString("letter_grade");

                    boolean isEval = rs.getBoolean("evaluated");
                    if (hasMarks && marks >= 0) {
                        isEval = true;
                    }
                    res.setEvaluated(isEval);

                    if (isEval) {
                        res.setGradePoint(gp > 0 ? gp : CourseResult.calculateGradePoint(marks));
                        res.setLetterGrade((lg != null && !lg.trim().isEmpty() && !"-".equals(lg)) ? lg : CourseResult.calculateLetterGrade(marks));
                    } else {
                        res.setGradePoint(0.00);
                        res.setLetterGrade("-");
                    }

                    res.setPublished(rs.getBoolean("published"));

                    String sem = rs.getString("semester");
                    res.setSemester((sem != null && !sem.trim().isEmpty()) ? sem : "FALL 2025");

                    String enrolledBy = rs.getString("enrolled_by");
                    res.setEnrolledBy(enrolledBy != null ? enrolledBy : "ADMIN");

                    list.add(res);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course results for student id " + studentId + ": " + e.getMessage());
        }
        return list;
    }

    public List<CourseResult> getCourseResultsForDepartment(String studentUid, String department) {
        int studentId = resolveStudentId(studentUid);
        return getCourseResultsForDepartment(studentId, department);
    }

    public List<CourseResult> getResultsByStudentId(int studentId) {
        List<CourseResult> list = new ArrayList<>();
        if (studentId <= 0) return list;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_STUDENT_RESULTS)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResult(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching results for student id " + studentId + ": " + e.getMessage());
        }
        return list;
    }

    public List<CourseResult> getResultsByStudentUid(String studentUid) {
        int studentId = resolveStudentId(studentUid);
        return getResultsByStudentId(studentId);
    }

    public List<CourseResult> getPublishedResultsByStudentId(int studentId) {
        List<CourseResult> list = new ArrayList<>();
        if (studentId <= 0) return list;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PUBLISHED_STUDENT_RESULTS)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResult(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching published results for student id " + studentId + ": " + e.getMessage());
        }
        return list;
    }

    public List<CourseResult> getPublishedResultsByStudentUid(String studentUid) {
        int studentId = resolveStudentId(studentUid);
        return getPublishedResultsByStudentId(studentId);
    }

    public boolean saveResultsBatch(int studentId, List<CourseResult> results, boolean publish) {
        if (studentId <= 0 || results == null || results.isEmpty()) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPSERT_RESULT)) {
            conn.setAutoCommit(false);
            try {
                for (CourseResult cr : results) {
                    boolean isEvaluated = cr.isEvaluated() || (cr.getMarks() > 0.0);
                    boolean shouldPublishThisCourse = publish;

                    ps.setInt(1, studentId);
                    ps.setInt(2, cr.getCourseId());
                    ps.setString(3, cr.getSemester() != null ? cr.getSemester() : "FALL 2025");

                    if (isEvaluated) {
                        ps.setDouble(4, cr.getMarks());
                        ps.setDouble(5, cr.getGradePoint() > 0 ? cr.getGradePoint() : CourseResult.calculateGradePoint(cr.getMarks()));
                        ps.setString(6, cr.getLetterGrade() != null && !"-".equals(cr.getLetterGrade()) ? cr.getLetterGrade() : CourseResult.calculateLetterGrade(cr.getMarks()));
                        ps.setBoolean(7, shouldPublishThisCourse);
                        ps.setBoolean(8, true);
                    } else {
                        ps.setNull(4, java.sql.Types.DECIMAL);
                        ps.setDouble(5, 0.00);
                        ps.setString(6, "-");
                        ps.setBoolean(7, false); // Empty courses must NEVER be published!
                        ps.setBoolean(8, false);
                    }

                    ps.setString(9, cr.getEnrolledBy() != null ? cr.getEnrolledBy() : "ADMIN");
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error saving results batch for student id " + studentId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean saveResultsBatch(String studentUid, List<CourseResult> results, boolean publish) {
        int studentId = resolveStudentId(studentUid);
        return saveResultsBatch(studentId, results, publish);
    }

    public boolean updateSingleCoursePublish(int studentId, int courseId, boolean publish) {
        if (studentId <= 0 || courseId <= 0) return false;
        String sql = "UPDATE student_results SET published = ? WHERE student_id = ? AND course_id = ? AND evaluated = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, publish);
            ps.setInt(2, studentId);
            ps.setInt(3, courseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating single course publish status: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSingleCoursePublish(String studentUid, int courseId, boolean publish) {
        int studentId = resolveStudentId(studentUid);
        return updateSingleCoursePublish(studentId, courseId, publish);
    }

    public boolean updateCourseResultSemester(int studentId, int courseId, String semester) {
        if (studentId <= 0 || courseId <= 0 || semester == null || semester.trim().isEmpty()) return false;
        String sql = "UPDATE student_results SET semester = ? WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semester.trim());
            ps.setInt(2, studentId);
            ps.setInt(3, courseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating course semester: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCourseResultSemester(String studentUid, int courseId, String semester) {
        int studentId = resolveStudentId(studentUid);
        return updateCourseResultSemester(studentId, courseId, semester);
    }

    public int enrollCourses(int studentId, String semester, List<Integer> courseIds, String enrolledBy) {
        if (studentId <= 0 || courseIds == null || courseIds.isEmpty()) {
            return 0;
        }
        String sql = "INSERT INTO student_results (student_id, course_id, semester, enrolled_by, marks, grade_point, letter_grade, published, evaluated) " +
                     "VALUES (?, ?, ?, ?, NULL, 0.00, '-', FALSE, FALSE) " +
                     "ON DUPLICATE KEY UPDATE semester = VALUES(semester), enrolled_by = VALUES(enrolled_by)";
        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Integer cid : courseIds) {
                if (cid == null || cid <= 0) continue;
                ps.setInt(1, studentId);
                ps.setInt(2, cid);
                ps.setString(3, semester != null ? semester.trim() : "FALL 2025");
                ps.setString(4, enrolledBy != null ? enrolledBy.trim() : "ADMIN");
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error enrolling courses: " + e.getMessage());
            return 0;
        }
        return count;
    }

    public int enrollCourses(String studentUid, String semester, List<Integer> courseIds, String enrolledBy) {
        int studentId = resolveStudentId(studentUid);
        return enrollCourses(studentId, semester, courseIds, enrolledBy);
    }

    public int purgeUnevaluatedZeroRecords(int studentId) {
        if (studentId <= 0) return 0;
        String sql = "DELETE FROM student_results WHERE student_id = ? AND (marks IS NULL OR marks = 0.00) AND (evaluated = FALSE OR letter_grade = '-' OR letter_grade = 'F') AND published = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error purging unevaluated records: " + e.getMessage());
            return 0;
        }
    }

    public int purgeUnevaluatedZeroRecords(String studentUid) {
        int studentId = resolveStudentId(studentUid);
        return purgeUnevaluatedZeroRecords(studentId);
    }

    public double calculateAndSyncStudentCgpa(int studentId) {
        if (studentId <= 0) {
            return 0.00;
        }

        // Only compute CGPA from PUBLISHED and EVALUATED course results!
        List<CourseResult> results = getPublishedResultsByStudentId(studentId);
        double calculatedCgpa = CourseResult.computeOverallCgpa(results);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_STUDENT_CGPA)) {
            ps.setDouble(1, calculatedCgpa);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating student CGPA for id " + studentId + ": " + e.getMessage());
        }

        return calculatedCgpa;
    }

    public double calculateAndSyncStudentCgpa(String studentUid) {
        int studentId = resolveStudentId(studentUid);
        return calculateAndSyncStudentCgpa(studentId);
    }

    private CourseResult mapResult(ResultSet rs) throws SQLException {
        CourseResult res = new CourseResult();
        res.setId(rs.getInt("result_id"));
        int sId = rs.getInt("student_id");
        res.setStudentId(sId);
        res.setCourseId(rs.getInt("course_id"));
        res.setCourseCode(rs.getString("course_code"));
        res.setCourseName(rs.getString("course_name"));
        res.setDepartment(rs.getString("department"));
        res.setCreditHours(rs.getDouble("credit_hours"));

        Object mObj = rs.getObject("marks");
        boolean hasMarks = (mObj != null);
        double marks = hasMarks ? rs.getDouble("marks") : 0.0;
        res.setMarks(marks);

        res.setGradePoint(rs.getDouble("grade_point"));
        String lg = rs.getString("letter_grade");
        res.setLetterGrade(lg != null ? lg : "-");
        res.setPublished(rs.getBoolean("published"));

        try {
            boolean isEval = rs.getBoolean("evaluated");
            if (hasMarks && marks > 0) isEval = true;
            res.setEvaluated(isEval);
        } catch (SQLException ignored) {
            res.setEvaluated(hasMarks && marks > 0);
        }

        try {
            String sem = rs.getString("semester");
            res.setSemester(sem != null ? sem : "FALL 2025");
        } catch (SQLException ignored) {
            res.setSemester("FALL 2025");
        }

        try {
            String eb = rs.getString("enrolled_by");
            res.setEnrolledBy(eb != null ? eb : "ADMIN");
        } catch (SQLException ignored) {
            res.setEnrolledBy("ADMIN");
        }

        return res;
    }
}
