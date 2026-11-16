package com.studentmanagement.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.service.StudentServiceImpl;
import com.studentmanagement.service.UserService;
import com.studentmanagement.service.UserServiceImpl;
import com.studentmanagement.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {
        "/admin/dashboard",
        "/student/dashboard",
        "/students",
        "/add-student",
        "/edit-student",
        "/delete-student",
        "/api/students",
        "/api/students/*"
})
public class FrontControllerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final StudentService studentService;
    private final UserService userService;

    public FrontControllerServlet() {
        this(new StudentServiceImpl(), new UserServiceImpl());
    }

    public FrontControllerServlet(StudentService studentService) {
        this(studentService, new UserServiceImpl());
    }

    public FrontControllerServlet(StudentService studentService, UserService userService) {
        this.studentService = (studentService != null) ? studentService : new StudentServiceImpl();
        this.userService = (userService != null) ? userService : new UserServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if (path.startsWith("/api/students")) {
            JsonUtil.sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "API endpoints accept POST requests.");
            return;
        }

        User user = getUser(request);

        request.setAttribute("currentSemester", com.studentmanagement.util.SemesterUtil.getCurrentSemester());
        request.setAttribute("availableSemesters", com.studentmanagement.util.SemesterUtil.getAvailableSemesters());
        request.setAttribute("availableYears", com.studentmanagement.util.SemesterUtil.getAvailableYears());

        switch (path) {
            case "/admin/dashboard":
                if (user == null || !user.isStaff()) {
                    response.sendRedirect(request.getContextPath() + "/student/dashboard");
                    return;
                }
                request.setAttribute("students", studentService.getAllStudents());
                request.setAttribute("isAdmin", user.isAdmin());
                request.setAttribute("isStaff", user.isStaff());
                request.setAttribute("userRole", user.getRoleCode());
                request.setAttribute("roleName", user.getRoleName());
                request.setAttribute("badgeClass", user.getBadgeClass());
                request.setAttribute("canAddStudent", user.isCanAddStudent());
                request.setAttribute("canEditStudent", user.isCanEditStudent());
                request.setAttribute("canDeleteStudent", user.isCanDeleteStudent());
                request.setAttribute("canAssignCourses", user.isCanAssignCourses());
                request.setAttribute("canGradeResults", user.isCanGradeResults());
                request.setAttribute("canPublishResults", user.isCanPublishResults());
                if (studentService.getLastError() != null) {
                    request.setAttribute("dbError", studentService.getLastError());
                }
                request.getRequestDispatcher("/students.jsp").forward(request, response);
                break;

            case "/student/dashboard":
                request.setAttribute("students", studentService.getStudentsForUser(user));
                request.setAttribute("isAdmin", false);
                request.setAttribute("isStaff", false);
                request.setAttribute("userRole", user != null ? user.getRoleCode() : "STUDENT");
                request.setAttribute("roleName", user != null ? user.getRoleName() : "Student");
                request.setAttribute("badgeClass", "bg-primary");
                request.setAttribute("canAddStudent", false);
                request.setAttribute("canEditStudent", false);
                request.setAttribute("canDeleteStudent", false);
                request.setAttribute("canAssignCourses", false);
                request.setAttribute("canGradeResults", false);
                request.setAttribute("canPublishResults", false);
                if (studentService.getLastError() != null) {
                    request.setAttribute("dbError", studentService.getLastError());
                }
                request.getRequestDispatcher("/students.jsp").forward(request, response);
                break;

            case "/add-student":
                if (user == null || !user.isCanAddStudent()) {
                    response.sendRedirect(request.getContextPath() + (user != null && user.isStaff() ? "/admin/dashboard" : "/student/dashboard"));
                    return;
                }
                request.getRequestDispatcher("/add-student.jsp").forward(request, response);
                break;

            case "/edit-student":
                if (user == null || !user.isCanEditStudent()) {
                    response.sendRedirect(request.getContextPath() + (user != null && user.isStaff() ? "/admin/dashboard" : "/student/dashboard"));
                    return;
                }
                String editUid = request.getParameter("uid");
                Student student = null;
                if (editUid != null && !editUid.trim().isEmpty()) {
                    student = studentService.getStudentByUid(editUid.trim());
                }
                if (student == null) {
                    String idParam = request.getParameter("id");
                    if (idParam != null && !idParam.trim().isEmpty()) {
                        try {
                            int id = Integer.parseInt(idParam.trim());
                            student = studentService.getStudentById(id);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (student != null) {
                    request.setAttribute("student", student);
                    request.getRequestDispatcher("/edit-student.jsp").forward(request, response);
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                }
                break;

            case "/students":
            default:
                response.sendRedirect(request.getContextPath() + userService.getDashboardRoute(user));
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        User user = getUser(request);

        switch (path) {
            case "/api/students":
                handleApi(request, response, user);
                break;
            case "/add-student":
                handleAdd(request, response, user);
                break;
            case "/edit-student":
                handleEdit(request, response, user);
                break;
            case "/delete-student":
                handleDelete(request, response, user);
                break;
            default:
                if (JsonUtil.isAjax(request)) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + path);
                } else {
                    response.sendRedirect(request.getContextPath() + userService.getDashboardRoute(user));
                }
                break;
        }
    }

    private void handleApi(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        JsonObject json = JsonUtil.readJson(request);
        String action = JsonUtil.getString(request, json, "action");
        String uid = getUid(request, json);

        if ("stats".equalsIgnoreCase(action) || "true".equalsIgnoreCase(request.getParameter("stats"))) {
            JsonUtil.sendJson(response, HttpServletResponse.SC_OK, studentService.getStudentStatistics(user));
            return;
        }

        if (action == null || action.trim().isEmpty()) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "API action is required.");
            return;
        }

        switch (action.toLowerCase()) {
            case "list":
            case "all":
                boolean isStaff = user != null && user.isStaff();
                boolean isAdmin = user != null && user.isAdmin();
                List<Student> students = studentService.getStudentsForUser(user);
                List<Map<String, Object>> dtoList = new ArrayList<>();
                if (students != null) {
                    for (Student s : students) {
                        dtoList.add(JsonUtil.toStudentDto(s, isStaff));
                    }
                }
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("students", dtoList);
                result.put("total", dtoList.size());
                result.put("isAdmin", isAdmin);
                result.put("isStaff", isStaff);
                result.put("role", user != null ? user.getRoleCode() : "GUEST");
                result.put("roleName", user != null ? user.getRoleName() : "Guest");
                result.put("badgeClass", user != null ? user.getBadgeClass() : "bg-secondary");
                result.put("canAdd", user != null && user.isCanAddStudent());
                result.put("canEdit", user != null && user.isCanEditStudent());
                result.put("canDelete", user != null && user.isCanDeleteStudent());
                result.put("canAssign", user != null && user.isCanAssignCourses());
                result.put("canGrade", user != null && user.isCanGradeResults());
                result.put("canPublish", user != null && user.isCanPublishResults());
                JsonUtil.sendJson(response, HttpServletResponse.SC_OK, result);
                break;

            case "get":
            case "view":
                if (uid == null || uid.trim().isEmpty()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Student UID is required.");
                    return;
                }
                Student found = studentService.getStudentByUid(uid.trim());
                if (found == null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Student not found.");
                    return;
                }
                if (!studentService.canViewStudent(user, found)) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                    return;
                }
                JsonUtil.sendJson(response, HttpServletResponse.SC_OK,
                        Map.of("success", true, "student", JsonUtil.toStudentDto(found, user != null && user.isStaff())));
                break;

            case "add":
            case "create":
                if (user == null || !user.isCanAddStudent()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied. Only Administrator or Registrar can add students.");
                    return;
                }
                Student newStudent = JsonUtil.extractStudent(request, json);
                String addCgpa = JsonUtil.extractCgpa(request, json);
                String addErr = studentService.addStudent(newStudent, addCgpa);
                if (addErr != null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, addErr);
                } else {
                    JsonUtil.sendJson(response, HttpServletResponse.SC_OK,
                            Map.of("success", true, "message", "Student registered successfully!"));
                }
                break;

            case "edit":
            case "update":
                if (user == null || !user.isCanEditStudent()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied. Only Administrator or Registrar can edit students.");
                    return;
                }
                Student editStudent = JsonUtil.extractStudent(request, json);
                String editCgpa = JsonUtil.extractCgpa(request, json);
                String editErr = studentService.updateStudent(editStudent, editCgpa);
                if (editErr != null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, editErr);
                } else {
                    JsonUtil.sendSuccess(response, "Student updated successfully!");
                }
                break;

            case "delete":
            case "remove":
                if (user == null || !user.isCanDeleteStudent()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied. Only Administrator can delete students.");
                    return;
                }
                String delErr = studentService.deleteStudentByUid(uid);
                if (delErr != null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, delErr);
                } else {
                    JsonUtil.sendSuccess(response, "Student deleted successfully.");
                }
                break;

            case "get_results":
            case "results":
                if (uid == null || uid.trim().isEmpty()) {
                    uid = getUid(request, json);
                }
                if (uid == null || uid.trim().isEmpty()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Student UID is required.");
                    return;
                }
                Student targetStudent = studentService.getStudentByUid(uid.trim());
                if (targetStudent == null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Student not found.");
                    return;
                }
                if (!studentService.canViewStudent(user, targetStudent)) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                    return;
                }
                List<com.studentmanagement.beans.CourseResult> resultsList = studentService.getCourseResultsForStudent(user, uid.trim());
                List<Map<String, Object>> resultDtos = new ArrayList<>();
                boolean allPublished = !resultsList.isEmpty();
                for (com.studentmanagement.beans.CourseResult cr : resultsList) {
                    resultDtos.add(JsonUtil.toCourseResultDto(cr));
                    if (!cr.isPublished()) {
                        allPublished = false;
                    }
                }
                boolean canGrade = user != null && user.isCanGradeResults();
                boolean canPublish = user != null && user.isCanPublishResults();
                double currentCgpa = targetStudent.getCgpa();

                Map<String, Object> resultsResponse = new HashMap<>();
                resultsResponse.put("success", true);
                resultsResponse.put("student", JsonUtil.toStudentDto(targetStudent, user != null && user.isStaff()));
                resultsResponse.put("results", resultDtos);
                resultsResponse.put("totalCourses", resultDtos.size());
                resultsResponse.put("calculatedCgpa", currentCgpa);
                resultsResponse.put("isPublished", allPublished);
                resultsResponse.put("canEdit", canGrade);
                resultsResponse.put("canPublish", canPublish);
                resultsResponse.put("canDeleteCourse", canGrade);
                resultsResponse.put("role", user != null ? user.getRoleCode() : "STUDENT");
                resultsResponse.put("roleName", user != null ? user.getRoleName() : "Student");
                resultsResponse.put("currentSemester", com.studentmanagement.util.SemesterUtil.getCurrentSemester());
                resultsResponse.put("upcomingSemester", com.studentmanagement.util.SemesterUtil.getUpcomingSemester());
                resultsResponse.put("currentSemesterDuration", com.studentmanagement.util.SemesterUtil.getSemesterDuration(com.studentmanagement.util.SemesterUtil.getCurrentSemester()));
                resultsResponse.put("upcomingSemesterDuration", com.studentmanagement.util.SemesterUtil.getSemesterDuration(com.studentmanagement.util.SemesterUtil.getUpcomingSemester()));

                JsonUtil.sendJson(response, HttpServletResponse.SC_OK, resultsResponse);
                break;

            case "save_results":
            case "update_results":
                if (user == null || !user.isCanGradeResults()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied. Only Faculty and Administrator can input course marks.");
                    return;
                }
                if (uid == null || uid.trim().isEmpty()) {
                    uid = getUid(request, json);
                }
                if (uid == null || uid.trim().isEmpty()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Student UID is required.");
                    return;
                }
                Student studentObj = studentService.getStudentByUid(uid.trim());
                if (studentObj == null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Student not found.");
                    return;
                }
                String studentJoiningSemester = studentObj.getSemester();
                String targetSemester = JsonUtil.getString(request, json, "semester");
                List<com.studentmanagement.beans.CourseResult> extractedResults = JsonUtil.extractCourseResults(json);
                if (extractedResults.isEmpty()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "No course results provided.");
                    return;
                }
                // Do NOT overwrite student joining semester in students table
                for (com.studentmanagement.beans.CourseResult cr : extractedResults) {
                    if (cr.getSemester() == null || cr.getSemester().trim().isEmpty()) {
                        cr.setSemester(targetSemester != null && !targetSemester.trim().isEmpty() ? targetSemester.trim() : studentJoiningSemester);
                    }
                }
                boolean shouldPublish = (json != null && json.has("publish")) ? json.get("publish").getAsBoolean() : true;
                String saveResultErr = studentService.saveAndPublishResults(user, uid.trim(), extractedResults, shouldPublish);
                if (saveResultErr != null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, saveResultErr);
                } else {
                    double newCgpa = studentService.recalculateCgpa(uid.trim());
                    Map<String, Object> saveResp = new HashMap<>();
                    saveResp.put("success", true);
                    saveResp.put("message", shouldPublish ? "Results published successfully and CGPA updated!" : "Results saved successfully and CGPA updated!");
                    saveResp.put("calculatedCgpa", newCgpa);
                    saveResp.put("published", shouldPublish);
                    saveResp.put("semester", studentJoiningSemester);
                    JsonUtil.sendJson(response, HttpServletResponse.SC_OK, saveResp);
                }
                break;

            case "publish_single_course":
                if (user == null || !user.isCanPublishResults()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied. Only Administrator and Supervisor can publish courses.");
                    return;
                }
                if (uid == null || uid.trim().isEmpty()) {
                    uid = getUid(request, json);
                }
                if (uid == null || uid.trim().isEmpty() || json == null || !json.has("courseId")) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Student UID and Course ID are required.");
                    return;
                }
                int singleCourseId = json.get("courseId").getAsInt();
                boolean singlePublish = json.has("publish") ? json.get("publish").getAsBoolean() : true;
                String singleSem = JsonUtil.getString(request, json, "semester");
                if (singleSem != null && !singleSem.trim().isEmpty()) {
                    studentService.updateCourseResultSemester(user, uid.trim(), singleCourseId, singleSem.trim());
                }
                boolean singleUpdated = studentService.updateSingleCoursePublish(user, uid.trim(), singleCourseId, singlePublish);
                if (singleUpdated) {
                    double updatedCgpa = studentService.recalculateCgpa(uid.trim());
                    Map<String, Object> respMap = new HashMap<>();
                    respMap.put("success", true);
                    respMap.put("message", singlePublish ? "Course published successfully." : "Course unpublished.");
                    respMap.put("calculatedCgpa", updatedCgpa);
                    respMap.put("published", singlePublish);
                    if (singleSem != null) {
                        respMap.put("semester", singleSem.trim());
                    }
                    JsonUtil.sendJson(response, HttpServletResponse.SC_OK, respMap);
                } else {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to update course status. Make sure marks are evaluated first.");
                }
                break;

            case "update_course_semester":
                if (!studentService.canManageStudents(user)) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied.");
                    return;
                }
                if (uid == null || uid.trim().isEmpty() || json == null || !json.has("courseId")) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Student UID and Course ID are required.");
                    return;
                }
                int cId = json.get("courseId").getAsInt();
                String semToUpdate = JsonUtil.getString(request, json, "semester");
                if (semToUpdate == null || semToUpdate.trim().isEmpty()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Semester is required.");
                    return;
                }
                boolean semUpdated = studentService.updateCourseResultSemester(user, uid.trim(), cId, semToUpdate.trim());
                if (semUpdated) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("success", true);
                    m.put("message", "Course semester updated to " + semToUpdate);
                    m.put("semester", semToUpdate);
                    JsonUtil.sendJson(response, HttpServletResponse.SC_OK, m);
                } else {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to update course semester.");
                }
                break;

            case "update_student_semester":
                if (!studentService.canManageStudents(user)) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied.");
                    return;
                }
                if (uid == null || uid.trim().isEmpty()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Student UID is required.");
                    return;
                }
                String studentSemToUpdate = JsonUtil.getString(request, json, "semester");
                if (studentSemToUpdate == null || studentSemToUpdate.trim().isEmpty()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Semester is required.");
                    return;
                }
                boolean stuSemUpdated = studentService.updateStudentSemester(user, uid.trim(), studentSemToUpdate.trim());
                if (stuSemUpdated) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("success", true);
                    m.put("message", "Student active semester updated to " + studentSemToUpdate);
                    m.put("semester", studentSemToUpdate);
                    JsonUtil.sendJson(response, HttpServletResponse.SC_OK, m);
                } else {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to update student semester.");
                }
                break;

            case "purge_unevaluated":
                if (!studentService.canManageStudents(user)) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied.");
                    return;
                }
                if (uid == null || uid.trim().isEmpty()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Student UID is required.");
                    return;
                }
                int purgedCount = studentService.purgeUnevaluatedRecords(user, uid.trim());
                double purgedCgpa = studentService.recalculateCgpa(uid.trim());
                Map<String, Object> purgeResp = new HashMap<>();
                purgeResp.put("success", true);
                purgeResp.put("purgedCount", purgedCount);
                purgeResp.put("calculatedCgpa", purgedCgpa);
                purgeResp.put("message", "Cleaned " + purgedCount + " unevaluated placeholder records. CGPA synchronized.");
                JsonUtil.sendJson(response, HttpServletResponse.SC_OK, purgeResp);
                break;

            case "register_courses":
                if (uid == null || uid.trim().isEmpty()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Student UID is required.");
                    return;
                }
                String regSemester = JsonUtil.getString(request, json, "semester");
                List<Integer> regCourseIds = JsonUtil.extractCourseIds(json);
                String regError = studentService.enrollStudentCourses(user, uid.trim(), regSemester, regCourseIds);
                if (regError != null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, regError);
                } else {
                    Map<String, Object> regResp = new HashMap<>();
                    regResp.put("success", true);
                    regResp.put("message", "Successfully registered " + regCourseIds.size() + " courses for " + regSemester + "!");
                    JsonUtil.sendJson(response, HttpServletResponse.SC_OK, regResp);
                }
                break;

            case "assign_courses":
                if (user == null || !user.isCanAssignCourses()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied. Only Administrator, Registrar, or Faculty can assign courses.");
                    return;
                }
                String assignDept = JsonUtil.getString(request, json, "department");
                String assignSem = JsonUtil.getString(request, json, "semester");
                String assignTarget = JsonUtil.getString(request, json, "target");
                List<Integer> assignCourseIds = JsonUtil.extractCourseIds(json);

                String assignErr;
                if (assignTarget != null && !assignTarget.trim().isEmpty() && !"ALL".equalsIgnoreCase(assignTarget.trim())) {
                    assignErr = studentService.enrollStudentCourses(user, assignTarget.trim(), assignSem, assignCourseIds);
                } else {
                    assignErr = studentService.assignCoursesToDepartment(user, assignDept, assignSem, assignCourseIds);
                }

                if (assignErr != null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, assignErr);
                } else {
                    Map<String, Object> assignResp = new HashMap<>();
                    assignResp.put("success", true);
                    assignResp.put("message", "Courses successfully assigned for semester " + assignSem + "!");
                    JsonUtil.sendJson(response, HttpServletResponse.SC_OK, assignResp);
                }
                break;

            case "courses":
                String dept = JsonUtil.getString(request, json, "department");
                List<com.studentmanagement.beans.Course> deptCourses = studentService.getCoursesForDepartment(dept);
                Map<String, Object> coursesResp = new HashMap<>();
                coursesResp.put("success", true);
                coursesResp.put("department", dept);
                coursesResp.put("courses", deptCourses);
                JsonUtil.sendJson(response, HttpServletResponse.SC_OK, coursesResp);
                break;

            case "semester_info":
                Map<String, Object> semInfo = new HashMap<>();
                semInfo.put("success", true);
                semInfo.put("currentSemester", com.studentmanagement.util.SemesterUtil.getCurrentSemester());
                semInfo.put("upcomingSemester", com.studentmanagement.util.SemesterUtil.getUpcomingSemester());
                semInfo.put("currentSemesterDuration", com.studentmanagement.util.SemesterUtil.getSemesterDuration(com.studentmanagement.util.SemesterUtil.getCurrentSemester()));
                semInfo.put("upcomingSemesterDuration", com.studentmanagement.util.SemesterUtil.getSemesterDuration(com.studentmanagement.util.SemesterUtil.getUpcomingSemester()));
                semInfo.put("availableSemesters", com.studentmanagement.util.SemesterUtil.getAvailableSemesters());
                JsonUtil.sendJson(response, HttpServletResponse.SC_OK, semInfo);
                break;

            default:
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid action: " + action);
                break;
        }
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null || !user.isCanAddStudent()) {
            if (JsonUtil.isAjax(request)) {
                JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied. Only Administrator or Registrar can add students.");
            } else {
                response.sendRedirect(request.getContextPath() + (user != null && user.isStaff() ? "/admin/dashboard" : "/student/dashboard"));
            }
            return;
        }

        JsonObject json = JsonUtil.readJson(request);
        Student student = JsonUtil.extractStudent(request, json);
        String cgpaStr = JsonUtil.extractCgpa(request, json);

        String error = studentService.addStudent(student, cgpaStr);
        if (error != null) {
            if (JsonUtil.isAjax(request)) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, error);
            } else {
                request.setAttribute("errorMessage", error);
                request.setAttribute("enteredName", student.getName());
                request.setAttribute("enteredEmail", student.getEmail());
                request.setAttribute("enteredPhone", student.getPhone());
                request.setAttribute("enteredDepartment", student.getDepartment());
                request.setAttribute("enteredSemester", student.getSemester());
                request.setAttribute("enteredCgpa", cgpaStr);
                request.setAttribute("currentSemester", com.studentmanagement.util.SemesterUtil.getCurrentSemester());
                request.setAttribute("availableSemesters", com.studentmanagement.util.SemesterUtil.getAvailableSemesters());
                request.setAttribute("availableYears", com.studentmanagement.util.SemesterUtil.getAvailableYears());
                request.getRequestDispatcher("/add-student.jsp").forward(request, response);
            }
        } else {
            if (JsonUtil.isAjax(request)) {
                JsonUtil.sendSuccess(response, "Student registered successfully!");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            }
        }
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null || !user.isCanEditStudent()) {
            if (JsonUtil.isAjax(request)) {
                JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied. Only Administrator or Registrar can edit students.");
            } else {
                response.sendRedirect(request.getContextPath() + (user != null && user.isStaff() ? "/admin/dashboard" : "/student/dashboard"));
            }
            return;
        }

        JsonObject json = JsonUtil.readJson(request);
        Student student = JsonUtil.extractStudent(request, json);
        String cgpaStr = JsonUtil.extractCgpa(request, json);

        if ((student.getUid() == null || student.getUid().trim().isEmpty()) && student.getId() > 0) {
            Student existing = studentService.getStudentById(student.getId());
            if (existing != null) {
                student.setUid(existing.getUid());
            }
        }

        String error = studentService.updateStudent(student, cgpaStr);
        if (error != null) {
            if (JsonUtil.isAjax(request)) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, error);
            } else {
                request.setAttribute("errorMessage", error);
                request.setAttribute("student", student);
                request.setAttribute("currentSemester", com.studentmanagement.util.SemesterUtil.getCurrentSemester());
                request.setAttribute("availableSemesters", com.studentmanagement.util.SemesterUtil.getAvailableSemesters());
                request.setAttribute("availableYears", com.studentmanagement.util.SemesterUtil.getAvailableYears());
                request.getRequestDispatcher("/edit-student.jsp").forward(request, response);
            }
        } else {
            if (JsonUtil.isAjax(request)) {
                JsonUtil.sendSuccess(response, "Student updated successfully!");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            }
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        if (user == null || !user.isCanDeleteStudent()) {
            if (JsonUtil.isAjax(request)) {
                JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Permission denied. Only Administrator can delete students.");
            } else {
                response.sendRedirect(request.getContextPath() + (user != null && user.isStaff() ? "/admin/dashboard" : "/student/dashboard"));
            }
            return;
        }

        JsonObject json = JsonUtil.readJson(request);
        String uid = getUid(request, json);
        String error = studentService.deleteStudentByUid(uid);

        if (JsonUtil.isAjax(request)) {
            if (error != null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, error);
            } else {
                JsonUtil.sendSuccess(response, "Student deleted successfully.");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        }
    }

    private String getUid(HttpServletRequest request, JsonObject json) {
        String uid = JsonUtil.getString(request, json, "uid");
        if (uid == null || uid.trim().isEmpty()) {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.length() > 1) {
                uid = pathInfo.substring(1);
            }
        }
        if (uid == null || uid.trim().isEmpty()) {
            String idStr = JsonUtil.getString(request, json, "id");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    Student s = studentService.getStudentById(id);
                    if (s != null) {
                        uid = s.getUid();
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return uid;
    }

    private User getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (User) session.getAttribute("user") : null;
    }
}
