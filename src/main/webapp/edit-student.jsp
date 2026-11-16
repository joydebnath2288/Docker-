<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activePage" value="students" scope="request" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Student - Student Management System</title>
    <%@ include file="/WEB-INF/fragments/head.jsp" %>
</head>
<body>

    <%@ include file="/WEB-INF/fragments/navbar.jsp" %>

    <div class="container main-container">
        <div class="row justify-content-center">
            <div class="col-lg-7 col-md-9">

                <div class="custom-card shadow-sm position-relative">
                    <div class="d-flex align-items-center mb-4 pb-2 border-bottom">
                        <div class="bg-primary text-white p-2 rounded-3 me-3">
                            <i class="bi bi-pencil-square fs-4"></i>
                        </div>
                        <div>
                            <h3 class="fw-bold mb-0 text-dark">Edit Student Record</h3>
                            <p class="text-secondary mb-0 small" id="editSubtitle">
                                Updating information for Student <span class="badge bg-primary text-white border font-monospace px-2 py-1" id="displayStudentId">ID: #${student.id}</span>
                            </p>
                        </div>
                    </div>

                    <div id="alertContainer">
                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                                <i class="bi bi-exclamation-triangle-fill flex-shrink-0 me-2"></i>
                                <div>${errorMessage}</div>
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                    </div>

                    <form id="editStudentForm" action="${pageContext.request.contextPath}/edit-student" method="POST" novalidate>
                        <input type="hidden" id="uid" name="uid" value="${student.uid}">
                        <input type="hidden" id="id" name="id" value="${student.id}">

                        <div class="mb-3">
                            <label for="name" class="form-label">Full Name <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="name" name="name" value="${student.name}" required autofocus>
                            <div class="invalid-feedback" id="nameFeedback">Please enter student's full name.</div>
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">Email Address <span class="text-danger">*</span></label>
                            <input type="email" class="form-control" id="email" name="email" value="${student.email}" required>
                            <div class="invalid-feedback">Please provide a valid email address.</div>
                        </div>

                        <div class="mb-3">
                            <label for="phone" class="form-label">Phone Number <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="phone" name="phone" value="${student.phone}" required>
                            <div class="invalid-feedback" id="phoneFeedback">Please enter a valid phone number.</div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="department" class="form-label">Department <span class="text-danger">*</span></label>
                                <select class="form-select" id="department" name="department" required>
                                    <option value="" disabled ${empty student.department ? 'selected' : ''}>Select Department</option>
                                    <option value="BBA" ${student.department == 'BBA' ? 'selected' : ''}>BBA</option>
                                    <option value="CSE" ${student.department == 'CSE' ? 'selected' : ''}>CSE</option>
                                    <option value="LAW" ${student.department == 'LAW' ? 'selected' : ''}>LAW</option>
                                    <option value="PHARMACY" ${student.department == 'PHARMACY' ? 'selected' : ''}>PHARMACY</option>
                                    <option value="ENGLISH" ${student.department == 'ENGLISH' ? 'selected' : ''}>ENGLISH</option>
                                    <option value="CHEMISTRY" ${student.department == 'CHEMISTRY' ? 'selected' : ''}>CHEMISTRY</option>
                                    <option value="DATA SCIENCE" ${student.department == 'DATA SCIENCE' ? 'selected' : ''}>DATA SCIENCE</option>
                                </select>
                                <div class="invalid-feedback">Please specify department.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="cgpa" class="form-label">CGPA (0.00 - 4.00) <span class="text-danger">*</span></label>
                                <input type="number" step="0.01" min="0.00" max="4.00" class="form-control" id="cgpa" name="cgpa" value="${student.cgpa}" required>
                                <div class="invalid-feedback">CGPA must be a valid number between 0.00 and 4.00.</div>
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="semesterTerm" class="form-label">Semester Term <span class="text-danger">*</span></label>
                                <select class="form-select" id="semesterTerm" name="semesterTerm" required>
                                    <option value="" disabled>Select Term</option>
                                    <option value="SPRING">SPRING</option>
                                    <option value="SUMMER">SUMMER</option>
                                    <option value="FALL">FALL</option>
                                </select>
                                <div class="invalid-feedback">Please select semester term.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="semesterYear" class="form-label">Semester Year <span class="text-danger">*</span></label>
                                <select class="form-select" id="semesterYear" name="semesterYear" required>
                                    <option value="" disabled>Select Year</option>
                                    <c:forEach var="yr" items="${availableYears}">
                                        <option value="${yr}">${yr}</option>
                                    </c:forEach>
                                </select>
                                <div class="invalid-feedback">Please select semester year.</div>
                            </div>
                            <input type="hidden" id="semester" name="semester" value="${student.semester}">
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="username" class="form-label">Username</label>
                                <input type="text" class="form-control" id="username" name="username" value="${student.username}">
                            </div>
                            <div class="col-md-6">
                                <label for="password" class="form-label">Password</label>
                                <input type="password" class="form-control" id="password" name="password" placeholder="Leave blank to keep current password" autocomplete="new-password">
                            </div>
                        </div>

                        <div class="d-flex justify-content-end gap-2 pt-2 border-top">
                            <a href="${pageContext.request.contextPath}${sessionScope.user.staff ? '/admin/dashboard' : '/student/dashboard'}" class="btn btn-outline-secondary px-4">
                                Cancel
                            </a>
                            <button type="submit" id="submitBtn" class="btn btn-primary px-4 shadow-sm">
                                <i class="bi bi-save me-1"></i> Update Student
                            </button>
                        </div>
                    </form>
                </div>

            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/fragments/footer.jsp" %>
    <%@ include file="/WEB-INF/fragments/scripts.jsp" %>

    <script>
        $(document).ready(function () {
            App.setupStudentForm({
                formSelector: '#editStudentForm',
                action: 'edit',
                loadingText: 'Updating student...',
                originalBtnHtml: '<i class="bi bi-save me-1"></i> Update Student',
                contextPath: '${pageContext.request.contextPath}',
                redirectUrl: '${pageContext.request.contextPath}${sessionScope.user.staff ? "/admin/dashboard" : "/student/dashboard"}',
                defaultValue: '${not empty student.semester ? student.semester : currentSemester}'
            });
        });
    </script>
</body>
</html>