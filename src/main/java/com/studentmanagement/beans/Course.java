package com.studentmanagement.beans;

import java.io.Serializable;

public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String courseCode;
    private String courseName;
    private String department;
    private double creditHours;

    public Course() {
    }

    public Course(int id, String courseCode, String courseName, String department, double creditHours) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.department = department;
        this.creditHours = creditHours;
    }

    public Course(String courseCode, String courseName, String department, double creditHours) {
        this(0, courseCode, courseName, department, creditHours);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public String toString() {
        return courseCode + " - " + courseName + " (" + creditHours + " cr)";
    }
}
