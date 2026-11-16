<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activePage" value="add" scope="request" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Student - Student Management System</title>
    <%@ include file="/WEB-INF/fragments/head.jsp" %>
</head>
<body>

    <%@ include file="/WEB-INF/fragments/navbar.jsp" %>

    <div class="container main-container">
        <div class="row justify-content-center">
            <div class="col-lg-7 col-md-9">

                <div class="custom-card shadow-sm">
                    <div class="d-flex align-items-center mb-4 pb-2 border-bottom">
                        <div class="bg-primary text-white p-2 rounded-3 me-3">
                            <i class="bi bi-person-plus-fill fs-4"></i>
                        </div>
                        <div>
                            <h3 class="fw-bold mb-0 text-dark">Add New Student</h3>
                            <p class="text-secondary mb-0 small">Enter student details to register via REST API</p>
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

                    <form id="addStudentForm" action="${pageContext.request.contextPath}/add-student" method="POST" autocomplete="off" novalidate>
                        <div class="mb-3">
                            <label for="name" class="form-label">Full Name <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="name" name="name"
                                   placeholder="e.g. Rahim Ahmed" value="${enteredName}" required autofocus>
                            <div class="invalid-feedback" id="nameFeedback">Please enter student's full name.</div>
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">Email Address <span class="text-danger">*</span></label>
                            <input type="email" class="form-control" id="email" name="email"
                                   placeholder="e.g. rahim@gmail.com" value="${enteredEmail}" required>
                            <div class="invalid-feedback">Please provide a valid email address.</div>
                        </div>

                        <div class="mb-3">
                            <label for="phone" class="form-label">Phone Number <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="phone" name="phone"
                                   placeholder="e.g. 01711111111" maxlength="11" value="${enteredPhone}" required>
                            <div class="invalid-feedback" id="phoneFeedback">Please enter a valid 11-digit phone number.</div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="department" class="form-label">Department <span class="text-danger">*</span></label>
                                <select class="form-select" id="department" name="department" required>
                                    <option value="" disabled ${empty enteredDepartment ? 'selected' : ''}>Select Department</option>
                                    <option value="BBA" ${enteredDepartment == 'BBA' ? 'selected' : ''}>BBA</option>
                                    <option value="CSE" ${enteredDepartment == 'CSE' ? 'selected' : ''}>CSE</option>
                                    <option value="LAW" ${enteredDepartment == 'LAW' ? 'selected' : ''}>LAW</option>
                                    <option value="PHARMACY" ${enteredDepartment == 'PHARMACY' ? 'selected' : ''}>PHARMACY</option>
                                    <option value="ENGLISH" ${enteredDepartment == 'ENGLISH' ? 'selected' : ''}>ENGLISH</option>
                                    <option value="CHEMISTRY" ${enteredDepartment == 'CHEMISTRY' ? 'selected' : ''}>CHEMISTRY</option>
                                    <option value="DATA SCIENCE" ${enteredDepartment == 'DATA SCIENCE' ? 'selected' : ''}>DATA SCIENCE</option>
                                </select>
                                <div class="invalid-feedback">Please select student's department.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="cgpa" class="form-label">CGPA (0.00 - 4.00) <span class="text-danger">*</span></label>
                                <input type="number" step="0.01" min="0.00" max="4.00" class="form-control"
                                       id="cgpa" name="cgpa" placeholder="e.g. 3.75" value="${enteredCgpa}" required>
                                <div class="invalid-feedback">CGPA must be a decimal between 0.00 and 4.00.</div>
                            </div>
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="semesterTerm" class="form-label">Semester Term <span class="text-danger">*</span></label>
                                <select class="form-select" id="semesterTerm" name="semesterTerm" required>
                                    <option value="" disabled selected>Select Term</option>
                                    <option value="SPRING">SPRING</option>
                                    <option value="SUMMER">SUMMER</option>
                                    <option value="FALL">FALL</option>
                                </select>
                                <div class="invalid-feedback">Please select semester term.</div>
                            </div>
                            <div class="col-md-6">
                                <label for="semesterYear" class="form-label">Semester Year <span class="text-danger">*</span></label>
                                <select class="form-select" id="semesterYear" name="semesterYear" required>
                                    <option value="" disabled selected>Select Year</option>
                                    <c:forEach var="yr" items="${availableYears}">
                                        <option value="${yr}">${yr}</option>
                                    </c:forEach>
                                </select>
                                <div class="invalid-feedback">Please select semester year.</div>
                            </div>
                            <input type="hidden" id="semester" name="semester" value="${enteredSemester}">
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="username" class="form-label">Username</label>
                                <input type="text" class="form-control" id="username" name="username" placeholder="e.g. rahim" value="${enteredUsername}">
                            </div>
                            <div class="col-md-6">
                                <label for="password" class="form-label">Password</label>
                                <input type="password" class="form-control" id="password" name="password" placeholder="Enter password" value="${enteredPassword}">
                            </div>
                        </div>

                        <div class="d-flex justify-content-end gap-2 pt-2 border-top">
                            <a href="${pageContext.request.contextPath}${sessionScope.user.staff ? '/admin/dashboard' : '/student/dashboard'}" class="btn btn-outline-secondary px-4">
                                Cancel
                            </a>
                            <button type="submit" id="submitBtn" class="btn btn-primary px-4 shadow-sm">
                                <i class="bi bi-check-lg me-1"></i> Add Student
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
        $(document).ready(function() {
            App.setupStudentForm({
                formSelector: '#addStudentForm',
                action: 'add',
                loadingText: 'Saving student...',
                originalBtnHtml: '<i class="bi bi-check-lg me-1"></i> Add Student',
                contextPath: '${pageContext.request.contextPath}',
                redirectUrl: '${pageContext.request.contextPath}${sessionScope.user.staff ? "/admin/dashboard" : "/student/dashboard"}',
                defaultValue: '${not empty enteredSemester ? enteredSemester : currentSemester}'
            });
        });
    </script>
</body>
</html>
