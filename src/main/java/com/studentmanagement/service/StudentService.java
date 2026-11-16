package com.studentmanagement.service;

import java.util.List;
import java.util.Map;

import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;

public interface StudentService {

    List<Student> getAllStudents();

    List<Student> getStudentsForUser(User user);

    boolean isOwner(User user, Student student);

    boolean canManageStudents(User user);

    boolean canViewStudent(User user, Student student);

    Student getStudentByUid(String uid);

    Student getStudentById(int id);

    boolean deleteStudentById(int id);

    boolean addStudent(Student student);

    String addStudent(Student student, String cgpaStr);

    boolean updateStudent(Student student);

    String updateStudent(Student student, String cgpaStr);

    boolean deleteStudent(String uid);

    String deleteStudentByUid(String uid);

    boolean emailExists(String email);

    boolean emailExistsForOther(String email, String uid);

    String validateStudentSignup(String name, String email, String phone, String department);

    String validateStudentField(String field, String value, String uid);

    String validateStudentForAdd(Student student, String cgpaStr);

    String validateStudentForUpdate(Student student, String cgpaStr);

    Map<String, Object> getStudentStatistics(User user);

    List<com.studentmanagement.beans.Course> getCoursesForDepartment(String department);

    List<com.studentmanagement.beans.CourseResult> getCourseResultsForStudent(User user, String studentUid);

    String saveAndPublishResults(User adminUser, String studentUid, List<com.studentmanagement.beans.CourseResult> results, boolean publish);

    double recalculateCgpa(String studentUid);

    String enrollStudentCourses(User user, String studentUid, String semester, List<Integer> courseIds);

    String assignCoursesToDepartment(User adminUser, String department, String semester, List<Integer> courseIds);

    boolean updateSingleCoursePublish(User adminUser, String studentUid, int courseId, boolean publish);

    boolean updateStudentSemester(User adminUser, String studentUid, String semester);

    boolean updateCourseResultSemester(User adminUser, String studentUid, int courseId, String semester);

    int purgeUnevaluatedRecords(User adminUser, String studentUid);

    String getLastError();
}
