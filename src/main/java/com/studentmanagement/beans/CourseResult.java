package com.studentmanagement.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CourseResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int studentId;
    private String studentUid;
    private int courseId;
    private String courseCode;
    private String courseName;
    private String department;
    private double creditHours = 3.0;
    private double marks = 0.0;
    private double gradePoint = 0.0;
    private String letterGrade = "-";
    private boolean published = false;
    private boolean evaluated = false;
    private String semester = "FALL 2025";
    private String enrolledBy = "ADMIN";

    public CourseResult() {
    }

    public CourseResult(int courseId, double marks) {
        this.courseId = courseId;
        this.marks = marks;
        this.gradePoint = calculateGradePoint(marks);
        this.letterGrade = calculateLetterGrade(marks);
        this.evaluated = true;
    }

    public CourseResult(int studentId, int courseId, double marks, boolean published) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.marks = marks;
        this.gradePoint = calculateGradePoint(marks);
        this.letterGrade = calculateLetterGrade(marks);
        this.published = published;
        this.evaluated = true;
    }

    public CourseResult(String studentUid, int courseId, double marks, boolean published) {
        this.studentUid = studentUid;
        this.courseId = courseId;
        this.marks = marks;
        this.gradePoint = calculateGradePoint(marks);
        this.letterGrade = calculateLetterGrade(marks);
        this.published = published;
        this.evaluated = true;
    }

    public CourseResult(int id, int studentId, int courseId, double marks, double gradePoint, String letterGrade, boolean published) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.marks = marks;
        this.gradePoint = gradePoint;
        this.letterGrade = letterGrade;
        this.published = published;
        this.evaluated = true;
    }

    public CourseResult(int id, String studentUid, int courseId, double marks, double gradePoint, String letterGrade, boolean published) {
        this.id = id;
        this.studentUid = studentUid;
        this.courseId = courseId;
        this.marks = marks;
        this.gradePoint = gradePoint;
        this.letterGrade = letterGrade;
        this.published = published;
        this.evaluated = true;
    }

    public static double calculateGradePoint(double marks) {
        if (marks >= 90.0) return 4.00;
        if (marks >= 85.0) return 3.75;
        if (marks >= 80.0) return 3.50;
        if (marks >= 75.0) return 3.25;
        if (marks >= 70.0) return 3.00;
        if (marks >= 65.0) return 2.75;
        if (marks >= 60.0) return 2.50;
        if (marks >= 50.0) return 2.25;
        return 0.00;
    }

    public static String calculateLetterGrade(double marks) {
        if (marks >= 90.0) return "A+";
        if (marks >= 85.0) return "A";
        if (marks >= 80.0) return "B+";
        if (marks >= 75.0) return "B";
        if (marks >= 70.0) return "C+";
        if (marks >= 65.0) return "C";
        if (marks >= 60.0) return "D+";
        if (marks >= 50.0) return "D";
        return "F";
    }

    public static double computeOverallCgpa(List<CourseResult> results) {
        if (results == null || results.isEmpty()) {
            return 0.00;
        }

        double totalGradePoints = 0.0;
        double totalCredits = 0.0;

        for (CourseResult res : results) {
            if (!res.isPublished() || !res.isEvaluated()) {
                continue;
            }
            double credits = (res.getCreditHours() > 0) ? res.getCreditHours() : 3.0;
            totalGradePoints += (res.getGradePoint() * credits);
            totalCredits += credits;
        }

        if (totalCredits <= 0.0) {
            return 0.00;
        }

        double rawCgpa = totalGradePoints / totalCredits;
        return BigDecimal.valueOf(rawCgpa).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(String studentUid) {
        this.studentUid = studentUid;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(double creditHours) {
        this.creditHours = creditHours;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
        this.gradePoint = calculateGradePoint(marks);
        this.letterGrade = calculateLetterGrade(marks);
    }

    public double getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(double gradePoint) {
        this.gradePoint = gradePoint;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getEnrolledBy() {
        return enrolledBy;
    }

    public void setEnrolledBy(String enrolledBy) {
        this.enrolledBy = enrolledBy;
    }
}
