package com.studentmanagement.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.studentmanagement.beans.Course;
import com.studentmanagement.beans.CourseResult;
import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;
import com.studentmanagement.dao.CourseDAO;
import com.studentmanagement.dao.ResultDAO;
import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.dao.UserDAO;
import com.studentmanagement.util.PasswordUtil;

public class StudentServiceImpl implements StudentService {

    private final StudentDAO studentDAO;
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final ResultDAO resultDAO;

    public StudentServiceImpl() {
        this(new StudentDAO(), new UserDAO(), new CourseDAO(), new ResultDAO());
    }

    public StudentServiceImpl(StudentDAO studentDAO, UserDAO userDAO) {
        this(studentDAO, userDAO, new CourseDAO(), new ResultDAO());
    }

    public StudentServiceImpl(StudentDAO studentDAO, UserDAO userDAO, CourseDAO courseDAO, ResultDAO resultDAO) {
        this.studentDAO = (studentDAO != null) ? studentDAO : new StudentDAO();
        this.userDAO = (userDAO != null) ? userDAO : new UserDAO();
        this.courseDAO = (courseDAO != null) ? courseDAO : new CourseDAO();
        this.resultDAO = (resultDAO != null) ? resultDAO : new ResultDAO();
    }

    @Override
    public List<Student> getAllStudents() {
        return studentDAO.getAllStudents();
    }

    @Override
    public boolean canManageStudents(User user) {
        return user != null && user.isStaff();
    }

    @Override
    public boolean canViewStudent(User user, Student student) {
        if (user == null || student == null) {
            return false;
        }
        if (user.isStaff()) {
            return true;
        }
        return isOwner(user, student);
    }

    @Override
    public boolean isOwner(User user, Student student) {
        if (user == null || student == null) {
            return false;
        }
        if (user.isStaff()) {
            return true;
        }
        if (student.getUserId() != null && user.getId() > 0 && student.getUserId().intValue() == user.getId()) {
            return true;
        }
        if (student.getUser() != null && student.getUser().getUsername() != null) {
            if (student.getUser().getUsername().equalsIgnoreCase(user.getUsername())) {
                return true;
            }
        }
        if (user.getStudent() != null && student.getUid() != null) {
            if (student.getUid().equals(user.getStudent().getUid())) {
                return true;
            }
        }
        String currentUsername = user.getUsername();
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            return false;
        }
        currentUsername = currentUsername.trim();
        if (student.getUsername() != null && !student.getUsername().trim().isEmpty()) {
            return student.getUsername().trim().equalsIgnoreCase(currentUsername);
        }
        if (student.getEmail() != null && !student.getEmail().trim().isEmpty()) {
            return student.getEmail().trim().equalsIgnoreCase(currentUsername);
        }
        return false;
    }

    @Override
    public List<Student> getStudentsForUser(User user) {
        List<Student> all = studentDAO.getAllStudents();
        if (user != null && user.isStaff()) {
            for (Student s : all) {
                s.setPassword(null);
                if (s.getUser() != null) {
                    s.getUser().setPassword(null);
                }
            }
            return all;
        }
        List<Student> filtered = new ArrayList<>();
        if (user != null) {
            for (Student s : all) {
                if (isOwner(user, s)) {
                    s.setPassword(null);
                    if (s.getUser() != null) {
                        s.getUser().setPassword(null);
                    }
                    filtered.add(s);
                }
            }
        }
        return filtered;
    }

    @Override
    public Student getStudentByUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            return null;
        }
        return studentDAO.getStudentByUid(uid.trim());
    }

    @Override
    public Student getStudentById(int id) {
        if (id <= 0) {
            return null;
        }
        return studentDAO.getStudentById(id);
    }

    @Override
    public boolean deleteStudentById(int id) {
        if (id <= 0) {
            return false;
        }
        Student student = studentDAO.getStudentById(id);
        if (student != null) {
            return deleteStudent(student.getUid());
        }
        return false;
    }

    @Override
    public boolean addStudent(Student student) {
        if (student == null) {
            return false;
        }

        if (student.getUid() == null || student.getUid().trim().isEmpty()) {
            student.setUid(UUID.randomUUID().toString());
        }

        normalizeCredentials(student);

        String username = student.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = generateDefaultUsername(student);
            student.setUsername(username);
        }

        String pass = (student.getPassword() != null && !student.getPassword().isEmpty())
                ? PasswordUtil.hashPassword(student.getPassword())
                : PasswordUtil.hashPassword(UUID.randomUUID().toString().substring(0, 8));

        User newUser = new User(0, username, pass, 2, 1);
        student.setUser(newUser);
        newUser.setStudent(student);
        return studentDAO.addStudent(student);
    }

    private String generateDefaultUsername(Student student) {
        String base = null;
        if (student.getEmail() != null && student.getEmail().contains("@")) {
            base = student.getEmail().split("@")[0].replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        }
        if (base == null || base.isEmpty()) {
            if (student.getName() != null && !student.getName().trim().isEmpty()) {
                base = student.getName().trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
            } else {
                base = "student";
            }
        }
        String candidate = base;
        int counter = 1;
        while (userDAO.usernameExists(candidate)) {
            candidate = base + "_" + (System.currentTimeMillis() % 10000) + counter;
            counter++;
        }
        return candidate;
    }

    @Override
    public String addStudent(Student student, String cgpaStr) {
        if (student == null) {
            return "Student information is required.";
        }

        String validationError = validateStudentForAdd(student, cgpaStr);
        if (validationError != null) {
            return validationError;
        }

        try {
            student.setCgpa(Double.parseDouble(cgpaStr.trim()));
        } catch (Exception e) {
            return "Invalid CGPA format.";
        }

        if (!addStudent(student)) {
            String dbError = studentDAO.getLastError();
            return (dbError != null && !dbError.trim().isEmpty()) ? dbError : "Failed to add student to database.";
        }

        return null;
    }

    @Override
    public boolean updateStudent(Student student) {
        if (student == null || student.getUid() == null || student.getUid().trim().isEmpty()) {
            return false;
        }

        normalizeCredentials(student);

        Student existing = studentDAO.getStudentByUid(student.getUid().trim());
        if (existing != null && existing.getId() > 0) {
            student.setId(existing.getId());
        }

        String username = student.getUsername();
        String password = student.getPassword();

        if (username != null && !username.trim().isEmpty()) {
            if (student.getId() > 0) {
                User user = userDAO.findById(student.getId());
                if (user != null) {
                    user.setUsername(username.trim());
                    if (password != null && !password.trim().isEmpty()) {
                        user.setPassword(PasswordUtil.hashPassword(password.trim()));
                    }
                    userDAO.updateUser(user);
                    student.setUser(user);
                }
            } else if (!userDAO.usernameExists(username.trim())) {
                String pass = (password != null && !password.isEmpty())
                        ? PasswordUtil.hashPassword(password.trim())
                        : PasswordUtil.hashPassword(UUID.randomUUID().toString());
                User newUser = new User(0, username.trim(), pass, 2, 1);
                if (userDAO.createUser(newUser)) {
                    student.setId(newUser.getId());
                    student.setUser(newUser);
                    newUser.setStudent(student);
                }
            }
        }

        return studentDAO.updateStudent(student);
    }

    private void normalizeCredentials(Student student) {
        String rawUsername = student.getUsername();
        if (rawUsername != null && !rawUsername.trim().isEmpty()) {
            student.setUsername(rawUsername.trim());
            String rawPassword = student.getPassword();
            if (rawPassword != null && !rawPassword.trim().isEmpty()) {
                student.setPassword(rawPassword.trim());
            } else {
                student.setPassword(null);
            }
        } else {
            student.setUsername(null);
            student.setPassword(null);
            student.setUser(null);
        }
    }

    @Override
    public String updateStudent(Student student, String cgpaStr) {
        if (student == null || student.getUid() == null || student.getUid().trim().isEmpty()) {
            return "Student UID is required.";
        }

        String validationError = validateStudentForUpdate(student, cgpaStr);
        if (validationError != null) {
            return validationError;
        }

        try {
            student.setCgpa(Double.parseDouble(cgpaStr.trim()));
        } catch (Exception e) {
            return "Invalid CGPA format.";
        }

        if (!updateStudent(student)) {
            String dbError = studentDAO.getLastError();
            return (dbError != null && !dbError.trim().isEmpty()) ? dbError : "Failed to update student.";
        }

        return null;
    }

    @Override
    public boolean deleteStudent(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            return false;
        }
        Student student = studentDAO.getStudentByUid(uid.trim());
        boolean rowDeleted = studentDAO.deleteStudent(uid.trim());
        if (rowDeleted && student != null) {
            if (student.getId() > 0) {
                userDAO.deleteUserById(student.getId());
            }
            if (student.getUsername() != null && !student.getUsername().trim().isEmpty()) {
                userDAO.deleteUser(student.getUsername().trim());
            }
        }
        return rowDeleted;
    }

    @Override
    public String deleteStudentByUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            return "No student UID provided for deletion.";
        }

        if (!deleteStudent(uid.trim())) {
            String dbError = studentDAO.getLastError();
            return (dbError != null && !dbError.trim().isEmpty()) ? dbError : "Failed to delete student.";
        }

        return null;
    }

    @Override
    public boolean emailExists(String email) {
        return email != null && !email.trim().isEmpty() && studentDAO.emailExists(email.trim());
    }

    @Override
    public boolean emailExistsForOther(String email, String uid) {
        return email != null && uid != null && studentDAO.emailExistsForOther(email.trim(), uid.trim());
    }

    private static final String REGEX_NAME_NO_DIGITS = ".*\\d.*";
    private static final String REGEX_EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final String REGEX_PHONE_11_DIGITS = "^\\d{11}$";

    @Override
    public String validateStudentSignup(String name, String email, String phone, String department) {
        if (isEmpty(name) || isEmpty(email) || isEmpty(phone) || isEmpty(department)) {
            return "All fields are required.";
        }

        name = name.trim();
        email = email.trim();
        phone = phone.trim();

        if (name.matches(REGEX_NAME_NO_DIGITS)) {
            return "Name must not contain numbers.";
        }
        if (!email.matches(REGEX_EMAIL)) {
            return "Please enter a valid email address.";
        }
        if (!phone.matches(REGEX_PHONE_11_DIGITS)) {
            return "Phone number must be exactly 11 digits and contain no letters.";
        }

        return null;
    }

    @Override
    public String validateStudentField(String field, String value, String uid) {
        if (field == null || field.trim().isEmpty()) {
            return "Field parameter is missing.";
        }

        field = field.trim();
        value = value != null ? value.trim() : "";

        switch (field) {
            case "name":
                if (value.isEmpty()) return "Name cannot be empty.";
                if (value.matches(REGEX_NAME_NO_DIGITS)) return "Name must not contain numbers.";
                return null;
            case "email":
                if (value.isEmpty()) return "Email cannot be empty.";
                if (!value.matches(REGEX_EMAIL)) return "Please enter a valid email address.";
                if (uid != null && !uid.trim().isEmpty()) {
                    if (emailExistsForOther(value, uid.trim())) return "Email already exists. Same email will not be accepted.";
                } else {
                    if (emailExists(value)) return "Email already exists. Same email will not be accepted.";
                }
                return null;
            case "phone":
                if (value.isEmpty()) return "Phone cannot be empty.";
                if (!value.matches(REGEX_PHONE_11_DIGITS)) return "Phone number must be exactly 11 digits and contain no letters.";
                return null;
            case "department":
                if (value.isEmpty()) return "Department cannot be empty.";
                return null;
            case "cgpa":
                if (value.isEmpty()) return "Cgpa cannot be empty.";
                try {
                    double cgpa = Double.parseDouble(value);
                    if (cgpa < 0.00 || cgpa > 4.00) return "CGPA must be between 0.00 and 4.00.";
                } catch (NumberFormatException e) {
                    return "Invalid CGPA format.";
                }
                return null;
            case "username":
                if (value.isEmpty()) return "Username cannot be empty.";
                return null;
            case "password":
                if (value.isEmpty()) return "Password cannot be empty.";
                return null;
            case "semester":
                if (value.isEmpty()) return "Semester cannot be empty.";
                if (!com.studentmanagement.util.SemesterUtil.isValidSemester(value)) return "Invalid semester format.";
                return null;
            default:
                return "Unknown field: " + field;
        }
    }

    @Override
    public String validateStudentForAdd(Student student, String cgpaStr) {
        if (student == null) {
            return "Student information is required.";
        }
        return validateStudent(student, cgpaStr, null);
    }

    @Override
    public String validateStudentForUpdate(Student student, String cgpaStr) {
        if (student == null || isEmpty(student.getUid())) {
            return "Student UID is required.";
        }
        return validateStudent(student, cgpaStr, student.getUid().trim());
    }

    private String validateStudent(Student student, String cgpaStr, String uid) {
        String err = validateStudentField("name", student.getName(), uid);
        if (err != null) return err;
        err = validateStudentField("email", student.getEmail(), uid);
        if (err != null) return err;
        err = validateStudentField("phone", student.getPhone(), uid);
        if (err != null) return err;
        err = validateStudentField("department", student.getDepartment(), uid);
        if (err != null) return err;
        err = validateStudentField("cgpa", cgpaStr, uid);
        if (err != null) return err;

        if (!isEmpty(student.getSemester())) {
            err = validateStudentField("semester", student.getSemester(), uid);
            if (err != null) return err;
        }

        if (!isEmpty(student.getUsername())) {
            err = validateStudentField("username", student.getUsername(), uid);
            if (err != null) return err;
        }
        if (!isEmpty(student.getPassword())) {
            err = validateStudentField("password", student.getPassword(), uid);
            if (err != null) return err;
        }
        return null;
    }

    @Override
    public Map<String, Object> getStudentStatistics(User user) {
        List<Student> students = getStudentsForUser(user);
        int total = students.size();
        double highestCgpa = 0.0;
        double sumCgpa = 0.0;
        Map<String, Integer> deptCounts = new HashMap<>();

        for (Student s : students) {
            if (s.getCgpa() > highestCgpa) {
                highestCgpa = s.getCgpa();
            }
            sumCgpa += s.getCgpa();
            String dept = (s.getDepartment() != null) ? s.getDepartment().trim().toUpperCase() : "GENERAL";
            deptCounts.put(dept, deptCounts.getOrDefault(dept, 0) + 1);
        }

        double avgCgpa = total > 0 ? Math.round((sumCgpa / total) * 100.0) / 100.0 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("success", true);
        stats.put("total", total);
        stats.put("highestCgpa", highestCgpa);
        stats.put("avgCgpa", avgCgpa);
        stats.put("departmentCount", deptCounts.size());
        stats.put("departments", deptCounts);
        stats.put("currentSemester", com.studentmanagement.util.SemesterUtil.getCurrentSemester());
        stats.put("availableSemesters", com.studentmanagement.util.SemesterUtil.getAvailableSemesters());
        stats.put("isAdmin", user != null && user.isAdmin());
        stats.put("isStaff", user != null && user.isStaff());
        stats.put("role", user != null ? user.getRoleCode() : "STUDENT");
        stats.put("roleName", user != null ? user.getRoleName() : "Student");

        return stats;
    }

    @Override
    public List<Course> getCoursesForDepartment(String department) {
        return courseDAO.getCoursesByDepartment(department);
    }

    @Override
    public List<CourseResult> getCourseResultsForStudent(User user, String studentUid) {
        if (user == null || studentUid == null || studentUid.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Student student = studentDAO.getStudentByUid(studentUid.trim());
        if (student == null) {
            return new ArrayList<>();
        }

        if (!canViewStudent(user, student)) {
            return new ArrayList<>();
        }

        if (canManageStudents(user)) {
            return resultDAO.getCourseResultsForDepartment(student.getId(), student.getDepartment());
        } else {
            return resultDAO.getPublishedResultsByStudentId(student.getId());
        }
    }

    @Override
    public String saveAndPublishResults(User adminUser, String studentUid, List<CourseResult> results, boolean publish) {
        if (!canManageStudents(adminUser)) {
            return "Permission denied. Only administrator can publish or edit results.";
        }

        if (studentUid == null || studentUid.trim().isEmpty()) {
            return "Student UID is required.";
        }

        Student student = studentDAO.getStudentByUid(studentUid.trim());
        if (student == null) {
            return "Student not found.";
        }

        if (results == null || results.isEmpty()) {
            return "No course results provided.";
        }

        for (CourseResult cr : results) {
            if (cr.getMarks() < 0.0 || cr.getMarks() > 100.0) {
                return "Course marks must be between 0 and 100.";
            }
            if (cr.getGradePoint() < 0.0 || cr.getGradePoint() > 4.00) {
                return "Course grade point must be between 0.00 and 4.00.";
            }
        }

        boolean saved = resultDAO.saveResultsBatch(student.getId(), results, publish);
        if (!saved) {
            return "Failed to save course results to database.";
        }

        // Recalculate CGPA from all course results and sync with students table
        resultDAO.calculateAndSyncStudentCgpa(student.getId());
        return null;
    }

    @Override
    public double recalculateCgpa(String studentUid) {
        Student student = (studentUid != null) ? studentDAO.getStudentByUid(studentUid.trim()) : null;
        if (student != null) {
            return resultDAO.calculateAndSyncStudentCgpa(student.getId());
        }
        return resultDAO.calculateAndSyncStudentCgpa(studentUid);
    }

    @Override
    public String enrollStudentCourses(User user, String studentUid, String semester, List<Integer> courseIds) {
        if (studentUid == null || studentUid.trim().isEmpty()) {
            return "Student UID is required.";
        }
        Student student = studentDAO.getStudentByUid(studentUid.trim());
        if (student == null) {
            return "Student not found.";
        }
        if (!canViewStudent(user, student)) {
            return "Permission denied. You can only register courses for yourself.";
        }
        if (courseIds == null || courseIds.isEmpty()) {
            return "Please select at least one course to register.";
        }
        String enrolledBy = canManageStudents(user) ? "ADMIN" : "STUDENT";
        String sem = (semester != null && !semester.trim().isEmpty()) ? semester.trim() : com.studentmanagement.util.SemesterUtil.getUpcomingSemester();
        int count = resultDAO.enrollCourses(student.getId(), sem, courseIds, enrolledBy);
        if (count == 0) {
            return "Failed to register courses.";
        }
        return null;
    }

    @Override
    public String assignCoursesToDepartment(User adminUser, String department, String semester, List<Integer> courseIds) {
        if (!canManageStudents(adminUser)) {
            return "Permission denied. Only administrator can assign courses.";
        }
        if (department == null || department.trim().isEmpty()) {
            return "Department is required.";
        }
        if (courseIds == null || courseIds.isEmpty()) {
            return "Please select at least one course to assign.";
        }
        String sem = (semester != null && !semester.trim().isEmpty()) ? semester.trim() : com.studentmanagement.util.SemesterUtil.getCurrentSemester();

        List<Student> allStudents = studentDAO.getAllStudents();
        int assignedStudentCount = 0;
        for (Student s : allStudents) {
            if (department.trim().equalsIgnoreCase(s.getDepartment())) {
                resultDAO.enrollCourses(s.getId(), sem, courseIds, "ADMIN");
                assignedStudentCount++;
            }
        }
        if (assignedStudentCount == 0) {
            return "No students found in department " + department + " to assign courses.";
        }
        return null;
    }

    @Override
    public boolean updateSingleCoursePublish(User adminUser, String studentUid, int courseId, boolean publish) {
        if (!canManageStudents(adminUser)) return false;
        Student student = (studentUid != null) ? studentDAO.getStudentByUid(studentUid.trim()) : null;
        int sId = (student != null) ? student.getId() : resultDAO.resolveStudentId(studentUid);
        boolean updated = resultDAO.updateSingleCoursePublish(sId, courseId, publish);
        if (updated) {
            resultDAO.calculateAndSyncStudentCgpa(sId);
        }
        return updated;
    }

    @Override
    public boolean updateStudentSemester(User adminUser, String studentUid, String semester) {
        if (!canManageStudents(adminUser)) return false;
        if (studentUid == null || semester == null || semester.trim().isEmpty()) return false;
        return studentDAO.updateStudentSemester(studentUid, semester.trim());
    }

    @Override
    public boolean updateCourseResultSemester(User adminUser, String studentUid, int courseId, String semester) {
        if (!canManageStudents(adminUser)) return false;
        if (studentUid == null || courseId <= 0 || semester == null || semester.trim().isEmpty()) return false;
        Student student = studentDAO.getStudentByUid(studentUid.trim());
        int sId = (student != null) ? student.getId() : resultDAO.resolveStudentId(studentUid);
        return resultDAO.updateCourseResultSemester(sId, courseId, semester.trim());
    }

    @Override
    public int purgeUnevaluatedRecords(User adminUser, String studentUid) {
        if (!canManageStudents(adminUser)) return 0;
        Student student = (studentUid != null) ? studentDAO.getStudentByUid(studentUid.trim()) : null;
        int sId = (student != null) ? student.getId() : resultDAO.resolveStudentId(studentUid);
        int count = resultDAO.purgeUnevaluatedZeroRecords(sId);
        resultDAO.calculateAndSyncStudentCgpa(sId);
        return count;
    }

    @Override
    public String getLastError() {
        return studentDAO.getLastError();
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
