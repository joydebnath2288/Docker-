<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activePage" value="signup" scope="request" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Sign Up - Student Management System</title>
    <%@ include file="/WEB-INF/fragments/head.jsp" %>
</head>
<body>

    <%@ include file="/WEB-INF/fragments/navbar.jsp" %>

    <div class="container main-container py-4">
        <div class="row justify-content-center">
            <div class="col-lg-7 col-md-9">
                <div class="custom-card shadow-sm p-4">
                    <div class="text-center mb-4">
                        <div class="bg-primary text-white d-inline-block p-3 rounded-circle mb-3">
                            <i class="bi bi-mortarboard fs-1"></i>
                        </div>
                        <h3 class="fw-bold text-dark">Student Registration</h3>
                        <p class="text-secondary small">Create your student account to access your academic result</p>
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>${errorMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <form id="signupForm" action="${pageContext.request.contextPath}/signup" method="POST" autocomplete="off" novalidate>
                        <div class="mb-3">
                            <label for="name" class="form-label fw-semibold">Full Name <span class="text-danger">*</span></label>
                            <div class="input-group has-validation">
                                <span class="input-group-text"><i class="bi bi-person-badge"></i></span>
                                <input type="text" class="form-control" id="name" name="name" value="${enteredName}" placeholder="e.g. Nusrat Nite" required autofocus>
                                <div class="invalid-feedback" id="nameFeedback">Please enter full name (no numbers allowed).</div>
                            </div>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label for="email" class="form-label fw-semibold">Email Address <span class="text-danger">*</span></label>
                                <div class="input-group has-validation">
                                    <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                                    <input type="email" class="form-control" id="email" name="email" value="${enteredEmail}" placeholder="e.g. rahim@gmail.com" required>
                                    <div class="invalid-feedback" id="emailFeedback">Please enter a valid email address.</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label for="phone" class="form-label fw-semibold">Phone Number <span class="text-danger">*</span></label>
                                <div class="input-group has-validation">
                                    <span class="input-group-text"><i class="bi bi-telephone"></i></span>
                                    <input type="text" class="form-control" id="phone" name="phone" value="${enteredPhone}" placeholder="e.g. 01711111111" maxlength="11" required>
                                    <div class="invalid-feedback" id="phoneFeedback">Phone number must be exactly 11 digits and contain no letters.</div>
                                </div>
                            </div>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label for="department" class="form-label fw-semibold">Department <span class="text-danger">*</span></label>
                                <div class="input-group has-validation">
                                    <span class="input-group-text"><i class="bi bi-building"></i></span>
                                    <select class="form-select" id="department" name="department" required>
                                        <option value="">Select Department</option>
                                        <option value="BBA" ${enteredDepartment == 'BBA' ? 'selected' : ''}>BBA</option>
                                        <option value="CSE" ${enteredDepartment == 'CSE' ? 'selected' : ''}>CSE</option>
                                        <option value="LAW" ${enteredDepartment == 'LAW' ? 'selected' : ''}>LAW</option>
                                        <option value="PHARMACY" ${enteredDepartment == 'PHARMACY' ? 'selected' : ''}>PHARMACY</option>
                                        <option value="ENGLISH" ${enteredDepartment == 'ENGLISH' ? 'selected' : ''}>ENGLISH</option>
                                        <option value="CHEMISTRY" ${enteredDepartment == 'CHEMISTRY' ? 'selected' : ''}>CHEMISTRY</option>
                                        <option value="DATA SCIENCE" ${enteredDepartment == 'DATA SCIENCE' ? 'selected' : ''}>DATA SCIENCE</option>
                                    </select>
                                    <div class="invalid-feedback" id="departmentFeedback">Please select your department.</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label for="username" class="form-label fw-semibold">Username <span class="text-danger">*</span></label>
                                <div class="input-group has-validation">
                                    <span class="input-group-text"><i class="bi bi-person"></i></span>
                                    <input type="text" class="form-control" id="username" name="username" value="${enteredUsername}" placeholder="min 6 letters" required minlength="6">
                                    <div class="invalid-feedback" id="usernameFeedback">Username must be at least 6 letters.</div>
                                </div>
                            </div>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label for="semesterTerm" class="form-label fw-semibold">Semester Term <span class="text-danger">*</span></label>
                                <div class="input-group has-validation">
                                    <span class="input-group-text"><i class="bi bi-calendar3"></i></span>
                                    <select class="form-select" id="semesterTerm" name="semesterTerm" required>
                                        <option value="">Select Term</option>
                                        <option value="SPRING">SPRING</option>
                                        <option value="SUMMER">SUMMER</option>
                                        <option value="FALL">FALL</option>
                                    </select>
                                    <div class="invalid-feedback" id="semesterTermFeedback">Please select semester term.</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label for="semesterYear" class="form-label fw-semibold">Semester Year <span class="text-danger">*</span></label>
                                <div class="input-group has-validation">
                                    <span class="input-group-text"><i class="bi bi-calendar-event"></i></span>
                                    <select class="form-select" id="semesterYear" name="semesterYear" required>
                                        <option value="">Select Year</option>
                                        <c:forEach var="yr" items="${availableYears}">
                                            <option value="${yr}">${yr}</option>
                                        </c:forEach>
                                    </select>
                                    <div class="invalid-feedback" id="semesterYearFeedback">Please select semester year.</div>
                                </div>
                            </div>
                            <input type="hidden" id="semester" name="semester" value="${enteredSemester}">
                        </div>

                        <div class="row g-3 mb-4">
                            <div class="col-md-6">
                                <label for="password" class="form-label fw-semibold">Password <span class="text-danger">*</span></label>
                                <div class="input-group has-validation">
                                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                    <input type="password" class="form-control" id="password" name="password" placeholder="min 6 letters" required minlength="6" autocomplete="new-password">
                                    <button class="btn btn-outline-secondary btn-toggle-password" type="button" data-target="#password" title="Show/Hide Password">
                                        <i class="bi bi-eye"></i>
                                    </button>
                                    <div class="invalid-feedback" id="passwordFeedback">Password must be at least 6 letters.</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label for="confirmPassword" class="form-label fw-semibold">Confirm Password <span class="text-danger">*</span></label>
                                <div class="input-group has-validation">
                                    <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                                    <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" placeholder="confirm password" required minlength="6" autocomplete="new-password">
                                    <button class="btn btn-outline-secondary btn-toggle-password" type="button" data-target="#confirmPassword" title="Show/Hide Password">
                                        <i class="bi bi-eye"></i>
                                    </button>
                                    <div class="invalid-feedback" id="confirmPasswordFeedback">Passwords do not match.</div>
                                </div>
                            </div>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary py-2 fw-bold shadow-sm">
                                <i class="bi bi-person-check-fill me-1"></i> Register Student Account
                            </button>
                        </div>
                    </form>

                    <div class="text-center mt-4 pt-3 border-top">
                        <p class="mb-0 text-secondary">Already have an account? <a href="${pageContext.request.contextPath}/login" class="text-decoration-none fw-bold">Login here</a></p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/fragments/footer.jsp" %>
    <%@ include file="/WEB-INF/fragments/scripts.jsp" %>

    <script>
        $(document).ready(function() {
            App.initSemesterSelect({
                termSelector: '#semesterTerm',
                yearSelector: '#semesterYear',
                hiddenSelector: '#semester',
                defaultValue: '${not empty enteredSemester ? enteredSemester : currentSemester}'
            });

            $('#signupForm').on('submit', function(e) {
                let isValid = true;
                const nameVal = $('#name').val().trim();
                const emailVal = $('#email').val().trim();
                const phoneVal = $('#phone').val().trim();
                const deptVal = $('#department').val();
                const termVal = $('#semesterTerm').val();
                const yearVal = $('#semesterYear').val();
                const usernameVal = $('#username').val().trim();
                const passwordVal = $('#password').val();
                const confirmPasswordVal = $('#confirmPassword').val();

                if (!nameVal || /\d/.test(nameVal)) {
                    $('#nameFeedback').text(!nameVal ? "Please enter student's full name." : "Name must not contain numbers.");
                    $('#name').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#name').addClass('is-valid');
                }

                if (!emailVal || !App.emailRegex.test(emailVal)) {
                    $('#emailFeedback').text("Please enter a valid email address.");
                    $('#email').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#email').addClass('is-valid');
                }

                if (!phoneVal || !App.phoneRegex.test(phoneVal)) {
                    $('#phoneFeedback').text("Phone number must be exactly 11 digits and contain no letters.");
                    $('#phone').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#phone').addClass('is-valid');
                }

                if (!deptVal) {
                    $('#department').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#department').addClass('is-valid');
                }

                if (!termVal) {
                    $('#semesterTerm').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#semesterTerm').addClass('is-valid');
                }

                if (!yearVal) {
                    $('#semesterYear').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#semesterYear').addClass('is-valid');
                }

                if (termVal && yearVal) {
                    $('#semester').val(termVal.trim().toUpperCase() + ' ' + yearVal.trim());
                }

                if (!usernameVal || usernameVal.length < 6) {
                    $('#usernameFeedback').text("Username must be at least 6 letters.");
                    $('#username').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#username').addClass('is-valid');
                }

                if (!passwordVal || passwordVal.length < 6) {
                    $('#passwordFeedback').text("Password must be at least 6 letters.");
                    $('#password').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#password').addClass('is-valid');
                }

                if (!confirmPasswordVal || confirmPasswordVal !== passwordVal) {
                    $('#confirmPasswordFeedback').text(!confirmPasswordVal ? "Please confirm password." : "Passwords do not match.");
                    $('#confirmPassword').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#confirmPassword').addClass('is-valid');
                }

                if (!isValid) e.preventDefault();
            });
        });
    </script>
</body>
</html>
