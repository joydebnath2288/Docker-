<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activePage" value="login" scope="request" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Student Management System</title>
    <%@ include file="/WEB-INF/fragments/head.jsp" %>
</head>
<body>

    <%@ include file="/WEB-INF/fragments/navbar.jsp" %>

    <div class="container main-container">
        <div class="row justify-content-center">
            <div class="col-md-5">
                <div class="custom-card shadow-sm">
                    <div class="text-center mb-4">
                        <div class="bg-primary text-white d-inline-block p-3 rounded-circle mb-3">
                            <i class="bi bi-person-lock fs-1"></i>
                        </div>
                        <h3 class="fw-bold">Welcome Back</h3>
                        <p class="text-secondary">Login to manage students</p>
                    </div>

                    <c:if test="${not empty successMessage}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <i class="bi bi-check-circle-fill me-2"></i>${successMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>${errorMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <form id="loginForm" action="${pageContext.request.contextPath}/login" method="POST" novalidate>
                        <div class="mb-3">
                            <label for="username" class="form-label">Username <span class="text-danger">*</span></label>
                            <div class="input-group has-validation">
                                <span class="input-group-text"><i class="bi bi-person"></i></span>
                                <input type="text" class="form-control" id="username" name="username" value="${enteredUsername}" placeholder="Enter username" required autofocus>
                                <div class="invalid-feedback" id="usernameFeedback">Please enter username.</div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="password" class="form-label">Password <span class="text-danger">*</span></label>
                            <div class="input-group has-validation">
                                <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                <input type="password" class="form-control" id="password" name="password" placeholder="Enter password" required>
                                <button class="btn btn-outline-secondary btn-toggle-password" type="button" data-target="#password" title="Show/Hide Password">
                                    <i class="bi bi-eye"></i>
                                </button>
                                <div class="invalid-feedback" id="passwordFeedback">Please enter password.</div>
                            </div>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary py-2 fw-bold">Login</button>
                        </div>
                    </form>

                    <div class="mt-4 pt-3 border-top">
                        <div class="d-flex align-items-center justify-content-between mb-2">
                            <span class="small fw-bold text-uppercase text-muted"><i class="bi bi-people-fill me-1"></i>Test Roles:</span>
                            <span class="badge bg-light text-secondary border small">Click to autofill</span>
                        </div>
                        <div class="d-flex flex-wrap gap-1">
                            <button type="button" class="btn btn-sm btn-outline-danger btn-demo-role py-1 px-2" data-user="admin" data-pass="admin" title="Administrator: Full access">
                                <span class="badge bg-danger me-1">Admin</span>admin
                            </button>
                            <button type="button" class="btn btn-sm btn-outline-dark btn-demo-role py-1 px-2" data-user="supervisor" data-pass="supervisor123" title="Supervisor: Review and publish results">
                                <span class="badge bg-dark me-1">Supervisor</span>supervisor
                            </button>
                            <button type="button" class="btn btn-sm btn-outline-info text-dark btn-demo-role py-1 px-2" data-user="faculty" data-pass="faculty123" title="Faculty: Grade courses & evaluate">
                                <span class="badge bg-info text-dark me-1">Faculty</span>faculty
                            </button>
                            <button type="button" class="btn btn-sm btn-outline-success btn-demo-role py-1 px-2" data-user="registrar" data-pass="registrar123" title="Registrar: Admissions, add/edit students">
                                <span class="badge bg-success me-1">Registrar</span>registrar
                            </button>
                            <button type="button" class="btn btn-sm btn-outline-warning text-dark btn-demo-role py-1 px-2" data-user="accounts" data-pass="accounts123" title="Accounts: View directory and fee records">
                                <span class="badge bg-warning text-dark me-1">Accounts</span>accounts
                            </button>
                        </div>
                    </div>

                    <div class="text-center mt-3 pt-2">
                        <p class="mb-0 text-secondary">Student Account? <a href="${pageContext.request.contextPath}/signup" class="text-decoration-none fw-bold">Sign Up as Student</a></p>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/fragments/footer.jsp" %>
    <%@ include file="/WEB-INF/fragments/scripts.jsp" %>

    <script>
        $(document).ready(function() {
            $('.btn-demo-role').on('click', function() {
                const u = $(this).data('user');
                const p = $(this).data('pass');
                $('#username').val(u).removeClass('is-invalid').addClass('is-valid');
                $('#password').val(p).removeClass('is-invalid').addClass('is-valid');
                $('#usernameFeedback, #passwordFeedback').text('');
            });

            $('#loginForm').on('submit', function(e) {
                let isValid = true;
                const usernameVal = $('#username').val().trim();
                const passwordVal = $('#password').val();

                if (!usernameVal) {
                    $('#usernameFeedback').text("Please enter username.");
                    $('#username').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#username').addClass('is-valid');
                }

                if (!passwordVal) {
                    $('#passwordFeedback').text("Please enter password.");
                    $('#password').addClass('is-invalid');
                    isValid = false;
                } else {
                    $('#password').addClass('is-valid');
                }

                if (!isValid) e.preventDefault();
            });
        });
    </script>
</body>
</html>
