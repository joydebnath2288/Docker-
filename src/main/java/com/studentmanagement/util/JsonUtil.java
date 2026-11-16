package com.studentmanagement.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.studentmanagement.beans.Student;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JsonUtil {

    private static final Gson gson = new Gson();

    public static JsonObject readJson(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String body = sb.toString().trim();
                if (!body.isEmpty()) {
                    JsonElement el = JsonParser.parseString(body);
                    if (el.isJsonObject()) {
                        return el.getAsJsonObject();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing JSON: " + e.getMessage());
            }
        }
        return null;
    }

    public static Map<String, Object> toStudentDto(Student student, boolean isAdmin) {
        if (student == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", student.getId());
        map.put("uid", student.getUid());
        map.put("name", student.getName());
        map.put("email", student.getEmail());
        map.put("phone", student.getPhone());
        map.put("department", student.getDepartment());
        map.put("cgpa", student.getCgpa());
        map.put("semester", student.getSemester());
        if (isAdmin) {
            map.put("username", student.getUsername());
        }
        return map;
    }

    public static void sendJson(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        sendJson(response, statusCode, Map.of("success", false, "message", message));
    }

    public static void sendSuccess(HttpServletResponse response, String message) throws IOException {
        sendJson(response, HttpServletResponse.SC_OK, Map.of("success", true, "message", message));
    }

    public static String getString(HttpServletRequest request, JsonObject json, String key) {
        if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return request.getParameter(key);
    }

    public static boolean isAjax(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return "XMLHttpRequest".equalsIgnoreCase(xRequestedWith)
                || (accept != null && accept.contains("application/json"))
                || (uri != null && uri.contains("/api/"))
                || "true".equalsIgnoreCase(request.getParameter("ajax"));
    }

    public static Student extractStudent(HttpServletRequest request, JsonObject json) {
        Student student = new Student();
        student.setName(getString(request, json, "name"));
        student.setEmail(getString(request, json, "email"));
        student.setPhone(getString(request, json, "phone"));
        student.setDepartment(getString(request, json, "department"));

        String sem = getString(request, json, "semester");
        if (sem == null || sem.trim().isEmpty()) {
            String term = getString(request, json, "semesterTerm");
            String year = getString(request, json, "semesterYear");
            if (term != null && !term.trim().isEmpty() && year != null && !year.trim().isEmpty()) {
                sem = SemesterUtil.formatSemester(term, year);
            }
        }
        student.setSemester((sem != null && !sem.trim().isEmpty()) ? sem.trim().toUpperCase() : SemesterUtil.getCurrentSemester());

        student.setUsername(getString(request, json, "username"));
        student.setPassword(getString(request, json, "password"));

        String idStr = getString(request, json, "id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                student.setId(Integer.parseInt(idStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        String uid = getString(request, json, "uid");
        if (uid != null && !uid.trim().isEmpty()) {
            student.setUid(uid.trim());
        }

        if (student.getUsername() != null && student.getUsername().trim().isEmpty()) {
            student.setUsername(null);
        }
        if (student.getPassword() != null && student.getPassword().trim().isEmpty()) {
            student.setPassword(null);
        }
        return student;
    }

    public static String extractCgpa(HttpServletRequest request, JsonObject json) {
        return getString(request, json, "cgpa");
    }

    public static Map<String, Object> toCourseResultDto(com.studentmanagement.beans.CourseResult r) {
        if (r == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("studentId", r.getStudentId());
        map.put("courseId", r.getCourseId());
        map.put("courseCode", r.getCourseCode());
        map.put("courseName", r.getCourseName());
        map.put("department", r.getDepartment());
        map.put("creditHours", r.getCreditHours());
        map.put("marks", r.getMarks());
        map.put("gradePoint", r.getGradePoint());
        map.put("letterGrade", r.getLetterGrade());
        map.put("published", r.isPublished());
        map.put("evaluated", r.isEvaluated());
        map.put("semester", r.getSemester());
        map.put("enrolledBy", r.getEnrolledBy());
        return map;
    }

    public static java.util.List<com.studentmanagement.beans.CourseResult> extractCourseResults(JsonObject json) {
        java.util.List<com.studentmanagement.beans.CourseResult> list = new java.util.ArrayList<>();
        if (json != null && json.has("results") && json.get("results").isJsonArray()) {
            com.google.gson.JsonArray arr = json.getAsJsonArray("results");
            for (com.google.gson.JsonElement el : arr) {
                if (el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    int courseId = obj.has("courseId") ? obj.get("courseId").getAsInt() : 0;
                    double marks = obj.has("marks") ? obj.get("marks").getAsDouble() : 0.0;
                    boolean isEvaluated = obj.has("evaluated") ? obj.get("evaluated").getAsBoolean() : (marks > 0.0);
                    com.studentmanagement.beans.CourseResult cr = new com.studentmanagement.beans.CourseResult(courseId, marks);
                    cr.setEvaluated(isEvaluated);
                    if (obj.has("gradePoint")) {
                        cr.setGradePoint(obj.get("gradePoint").getAsDouble());
                    }
                    if (obj.has("letterGrade")) {
                        cr.setLetterGrade(obj.get("letterGrade").getAsString());
                    }
                    if (obj.has("published")) {
                        cr.setPublished(obj.get("published").getAsBoolean());
                    }
                    if (obj.has("semester")) {
                        cr.setSemester(obj.get("semester").getAsString());
                    }
                    if (obj.has("enrolledBy")) {
                        cr.setEnrolledBy(obj.get("enrolledBy").getAsString());
                    }
                    list.add(cr);
                }
            }
        }
        return list;
    }

    public static java.util.List<Integer> extractCourseIds(JsonObject json) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        if (json != null && json.has("courseIds") && json.get("courseIds").isJsonArray()) {
            com.google.gson.JsonArray arr = json.getAsJsonArray("courseIds");
            for (com.google.gson.JsonElement el : arr) {
                try {
                    list.add(el.getAsInt());
                } catch (Exception ignored) {}
            }
        }
        return list;
    }
}
