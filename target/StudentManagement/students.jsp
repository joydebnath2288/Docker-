<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="activePage" value="students" scope="request" />
<c:set var="isStaff" value="${sessionScope.user != null && sessionScope.user.staff}" scope="request" />
<c:set var="isAdmin" value="${sessionScope.user != null && sessionScope.user.admin}" scope="request" />
<c:set var="canAdd" value="${sessionScope.user != null && sessionScope.user.canAddStudent}" scope="request" />
<c:set var="canEdit" value="${sessionScope.user != null && sessionScope.user.canEditStudent}" scope="request" />
<c:set var="canDelete" value="${sessionScope.user != null && sessionScope.user.canDeleteStudent}" scope="request" />
<c:set var="canAssign" value="${sessionScope.user != null && sessionScope.user.canAssignCourses}" scope="request" />
<c:set var="canGrade" value="${sessionScope.user != null && sessionScope.user.canGradeResults}" scope="request" />
<c:set var="canPublish" value="${sessionScope.user != null && sessionScope.user.canPublishResults}" scope="request" />
<c:set var="userRoleName" value="${sessionScope.user != null ? sessionScope.user.roleName : 'User'}" scope="request" />
<c:set var="userBadgeClass" value="${sessionScope.user != null ? sessionScope.user.badgeClass : 'bg-secondary'}" scope="request" />
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:choose><c:when test="${isStaff}">Student Directory - ${userRoleName} Portal</c:when><c:otherwise>My Academic Result - Student Portal</c:otherwise></c:choose></title>
    <%@ include file="/WEB-INF/fragments/head.jsp" %>
</head>

<body>

    <div class="toast-container toast-container-custom">
        <div id="liveToast" class="toast toast-custom align-items-center" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex p-2">
                <div class="toast-body d-flex align-items-center">
                    <i id="toastIcon" class="bi me-2 fs-5"></i>
                    <span id="toastMessage" class="fw-medium"></span>
                </div>
                <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    </div>

    <%@ include file="/WEB-INF/fragments/navbar.jsp" %>

    <div class="container main-container">

        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
            <div>
                <c:choose>
                    <c:when test="${isStaff}">
                        <h2 class="card-title-custom mb-1">Student Directory <span class="badge ${userBadgeClass} fs-6 align-middle">${userRoleName}</span></h2>
                        <p class="text-secondary mb-0">Manage and view student records, academic results, and permissions.</p>
                    </c:when>
                    <c:otherwise>
                        <h2 class="card-title-custom mb-1">My Academic Result <span class="badge bg-primary fs-6 align-middle">Student Portal</span></h2>
                        <p class="text-secondary mb-0">Logged in as <strong>${sessionScope.user.username}</strong>. Viewing your personal academic record.</p>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="d-flex gap-2 mt-3 mt-md-0">
                <button type="button" id="btnRefresh" class="btn btn-outline-secondary px-3 py-2 shadow-sm d-flex align-items-center" title="Refresh">
                    <i class="bi bi-arrow-clockwise me-1" id="refreshIcon"></i>
                    <span>Refresh</span>
                </button>
                <c:if test="${canAssign}">
                    <button type="button" id="btnOpenAssignCoursesModal" class="btn btn-outline-primary px-3 py-2 shadow-sm d-flex align-items-center" title="Assign Courses to Department or Students">
                        <i class="bi bi-journal-plus me-1"></i>
                        <span>Assign Courses by Dept</span>
                    </button>
                </c:if>
                <c:if test="${canAdd}">
                    <a href="${pageContext.request.contextPath}/add-student" class="btn btn-primary px-3 py-2 shadow-sm d-flex align-items-center">
                        <i class="bi bi-plus-lg me-1"></i>
                        <span>Add New Student</span>
                    </a>
                </c:if>
                <c:if test="${!isStaff}">
                    <button type="button" id="btnOpenStudentRegisterModal" class="btn btn-success px-3 py-2 shadow-sm d-flex align-items-center" title="Choose Courses for Upcoming Semester">
                        <i class="bi bi-calendar-check me-1"></i>
                        <span>Register Courses for New Semester</span>
                    </button>
                </c:if>
            </div>
        </div>

        <c:if test="${isStaff}">
            <div class="row g-3 mb-4">
                <div class="col-6 col-lg-3">
                    <div class="stat-card d-flex align-items-center">
                        <div class="stat-icon bg-primary bg-opacity-10 text-primary me-3">
                            <i class="bi bi-people-fill"></i>
                        </div>
                        <div>
                            <div class="stat-value" id="statTotal">-</div>
                            <div class="stat-label">Total Records</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card d-flex align-items-center">
                        <div class="stat-icon bg-success bg-opacity-10 text-success me-3">
                            <i class="bi bi-award-fill"></i>
                        </div>
                        <div>
                            <div class="stat-value" id="statAvgCgpa">-</div>
                            <div class="stat-label">Average CGPA</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card d-flex align-items-center">
                        <div class="stat-icon bg-warning bg-opacity-10 text-warning me-3">
                            <i class="bi bi-star-fill"></i>
                        </div>
                        <div>
                            <div class="stat-value" id="statHighestCgpa">-</div>
                            <div class="stat-label">Highest CGPA</div>
                        </div>
                    </div>
                </div>
                <div class="col-6 col-lg-3">
                    <div class="stat-card d-flex align-items-center">
                        <div class="stat-icon bg-info bg-opacity-10 text-info me-3">
                            <i class="bi bi-building"></i>
                        </div>
                        <div>
                            <div class="stat-value" id="statDeptCount">-</div>
                            <div class="stat-label">Departments</div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <c:if test="${not empty dbError}">
            <div class="alert alert-warning alert-dismissible fade show d-flex align-items-center mb-4 border-warning shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill fs-4 text-warning flex-shrink-0 me-3"></i>
                <div>
                    <h6 class="alert-heading fw-bold mb-1">Database Notice</h6>
                    <div>${dbError}</div>
                </div>
                <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${isStaff}">
            <div class="custom-card mb-4 p-3 shadow-sm">
                <div class="row g-2 align-items-center">
                    <div class="col-md-5">
                        <div class="search-wrapper">
                            <i class="bi bi-search"></i>
                            <input type="text" id="searchInput" class="form-control search-input"
                                placeholder="Live search by name, department, semester...">
                        </div>
                    </div>
                    <div class="col-md-7 d-flex flex-wrap gap-2 justify-content-md-end align-items-center">
                        <div class="d-flex align-items-center gap-1">
                            <label for="deptFilter" class="form-label mb-0 text-nowrap small text-muted">Department:</label>
                            <select id="deptFilter" class="form-select form-select-sm" style="min-width: 140px; max-width: 180px;">
                                <option value="ALL">All Departments</option>
                                <option value="BBA">BBA</option>
                                <option value="CSE">CSE</option>
                                <option value="LAW">LAW</option>
                                <option value="PHARMACY">PHARMACY</option>
                                <option value="ENGLISH">ENGLISH</option>
                                <option value="CHEMISTRY">CHEMISTRY</option>
                                <option value="DATA SCIENCE">DATA SCIENCE</option>
                            </select>
                        </div>
                        <div class="d-flex align-items-center gap-1">
                            <label for="semesterFilter" class="form-label mb-0 text-nowrap small text-muted">Semester:</label>
                            <select id="semesterFilter" class="form-select form-select-sm" style="min-width: 150px; max-width: 190px;">
                                <option value="ALL">All Semesters</option>
                                <c:forEach var="sem" items="${availableSemesters}">
                                    <option value="${sem}">${sem}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <div class="custom-card p-0 overflow-hidden position-relative">
            <div class="table-responsive">
                <table class="table table-hover table-custom mb-0" id="studentsTable">
                    <thead>
                        <tr>
                            <th class="ps-4">ID</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Department</th>
                            <th>Semester</th>
                            <th>CGPA</th>
                            <c:if test="${isStaff}">
                                <th>Username</th>
                                <th class="text-end pe-4">Actions</th>
                            </c:if>
                            <c:if test="${!isStaff}">
                                <th class="text-end pe-4">Grade Sheet</th>
                            </c:if>
                        </tr>
                    </thead>
                    <tbody id="studentTableBody">
                        <c:if test="${empty students}">
                            <tr id="emptyStateRow">
                                <td colspan="${isStaff ? 9 : 8}" class="text-center py-5 text-secondary">
                                    <i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i>
                                    <span>No student records found.</span>
                                    <c:if test="${canAdd}">
                                        <div class="mt-2">
                                            <a href="${pageContext.request.contextPath}/add-student" class="btn btn-sm btn-outline-primary">
                                                Add the first student
                                            </a>
                                        </div>
                                    </c:if>
                                </td>
                            </tr>
                        </c:if>

                        <c:forEach var="student" items="${students}">
                            <tr id="student-row-${student.id}" class="student-item" data-id="${student.id}" data-uid="${student.uid}"
                                data-name="${student.name}" data-username="${student.username}" data-email="${student.email}"
                                data-phone="${student.phone}" data-dept="${student.department}" data-semester="${student.semester}">
                                <td class="ps-4 fw-bold text-primary font-monospace">#${student.id}</td>
                                <td class="fw-semibold text-dark student-name">${student.name}</td>
                                <td>
                                    <a href="mailto:${student.email}" class="text-decoration-none text-muted">
                                        <i class="bi bi-envelope me-1"></i>${student.email}
                                    </a>
                                </td>
                                <td>
                                    <span class="text-muted"><i class="bi bi-telephone me-1"></i>${student.phone}</span>
                                </td>
                                <td>
                                    <span class="badge-dept">${student.department}</span>
                                </td>
                                <td>
                                    <span class="badge-semester">${student.semester}</span>
                                </td>
                                <td>
                                    <span class="badge-cgpa">${student.cgpa}</span>
                                </td>
                                <c:if test="${isStaff}">
                                    <td>
                                        <span class="badge bg-secondary font-monospace px-2 py-1">${student.username}</span>
                                    </td>
                                    <td class="text-end pe-4">
                                        <div class="btn-group" role="group">
                                            <button type="button" class="btn btn-sm btn-outline-success btn-results"
                                                data-id="${student.id}" data-uid="${student.uid}" data-name="${student.name}" data-dept="${student.department}" data-semester="${student.semester}" title="Manage &amp; View Results">
                                                <i class="bi bi-mortarboard-fill me-1"></i>Results
                                            </button>
                                            <c:if test="${canEdit}">
                                                <a href="${pageContext.request.contextPath}/edit-student?uid=${student.uid}"
                                                    class="btn btn-sm btn-outline-primary" title="Edit Student">
                                                    <i class="bi bi-pencil-square me-1"></i>Edit
                                                </a>
                                            </c:if>
                                            <c:if test="${canDelete}">
                                                <button type="button" class="btn btn-sm btn-outline-danger btn-delete"
                                                    data-id="${student.id}" data-uid="${student.uid}" data-name="${student.name}" title="Delete Student">
                                                    <i class="bi bi-trash3 me-1"></i>Delete
                                                </button>
                                            </c:if>
                                        </div>
                                    </td>
                                </c:if>
                                <c:if test="${!isStaff}">
                                    <td class="text-end pe-4">
                                        <button type="button" class="btn btn-sm btn-outline-primary btn-results"
                                            data-id="${student.id}" data-uid="${student.uid}" data-name="${student.name}" data-dept="${student.department}" data-semester="${student.semester}" title="View Academic Results">
                                            <i class="bi bi-file-earmark-text me-1"></i>View Grades
                                        </button>
                                    </td>
                                </c:if>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

    </div>

    <c:if test="${canDelete}">
        <div class="modal fade" id="deleteConfirmModal" tabindex="-1" aria-labelledby="deleteModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content rounded-4 border-0 shadow">
                    <div class="modal-header border-bottom-0 pb-0">
                        <h5 class="modal-title fw-bold text-danger" id="deleteModalLabel">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>Confirm Deletion
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body py-4">
                        <p class="mb-1 text-dark">Are you sure you want to delete this student?</p>
                        <p class="fw-bold fs-5 text-primary mb-2" id="deleteStudentNameDisplay"></p>
                        <p class="small text-muted mb-0">This will permanently remove the record from the database.</p>
                    </div>
                    <div class="modal-footer border-top-0 pt-0">
                        <button type="button" class="btn btn-light px-4" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" id="confirmDeleteBtn" class="btn btn-danger px-4">
                            <i class="bi bi-trash3 me-1"></i> Delete Student
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <!-- Academic Results & Grade Sheet Modal -->
    <div class="modal fade" id="resultsModal" tabindex="-1" aria-labelledby="resultsModalLabel" aria-hidden="true" data-bs-backdrop="static">
        <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
            <div class="modal-content rounded-4 border-0 shadow">
                <div class="modal-header bg-light border-bottom py-3">
                    <div class="d-flex align-items-center gap-2">
                        <div class="stat-icon bg-primary bg-opacity-10 text-primary p-2 rounded-circle" style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center;">
                            <i class="bi bi-mortarboard-fill fs-5"></i>
                        </div>
                        <div>
                            <h5 class="modal-title fw-bold mb-0" id="resultsModalLabel">
                                <c:choose>
                                    <c:when test="${isStaff}">Academic Results &amp; Grade Management</c:when>
                                    <c:otherwise>Official Academic Grade Sheet</c:otherwise>
                                </c:choose>
                            </h5>
                            <span class="text-muted small">
                                <c:choose>
                                    <c:when test="${isStaff}">View course marks, calculated weighted CGPA, and publication status.</c:when>
                                    <c:otherwise>View your official course grades, credits earned, and calculated CGPA.</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <!-- Student Info Summary Header Card -->
                    <div class="card bg-body-tertiary border-0 rounded-3 p-3 mb-3">
                        <div class="row g-3 align-items-center">
                            <div class="col-md-3">
                                <small class="text-muted d-block">Student Name</small>
                                <span class="fw-bold fs-6 text-dark" id="modalStudentName">-</span>
                                <div class="small text-secondary" id="modalStudentUid">-</div>
                            </div>
                            <div class="col-6 col-md-3">
                                <small class="text-muted d-block">Department &amp; Joining Semester</small>
                                <div class="d-flex align-items-center gap-1 flex-wrap">
                                    <span class="badge-dept" id="modalStudentDept">-</span>
                                    <span class="badge-semester" id="modalStudentSem">-</span>
                                </div>
                                <div class="mt-1 d-flex align-items-center gap-1 flex-wrap">
                                    <span class="badge bg-info-subtle text-info border border-info-subtle small" id="modalSemDuration">
                                        <i class="bi bi-clock-history me-1"></i>4 Months Semester
                                    </span>
                                </div>
                            </div>
                            <div class="col-6 col-md-3">
                                <small class="text-muted d-block">Overall CGPA (Weighted)</small>
                                <div class="d-flex align-items-baseline gap-2">
                                    <span class="fs-4 fw-bolder text-primary" id="modalCgpaDisplay">0.00</span>
                                    <span class="text-muted small">/ 4.00</span>
                                </div>
                                <div class="small text-muted mt-1" id="modalEvaluatedCount">0 Evaluated Courses</div>
                            </div>
                            <div class="col-md-3 text-md-end">
                                <small class="text-muted d-block mb-1">Publication Status</small>
                                <span id="modalPublishStatusBadge" class="badge bg-secondary px-3 py-2 fs-7">-</span>
                            </div>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${canGrade && canPublish}">
                            <div class="alert alert-primary py-2 px-3 small d-flex align-items-center gap-2 mb-3">
                                <i class="bi bi-info-circle-fill text-primary flex-shrink-0"></i>
                                <div>
                                    <strong>Admin Control:</strong> Enter marks (0–100) for courses and publish official results. Empty courses will not be published or lower student CGPA. Every semester duration is <strong>4 months</strong>.
                                </div>
                            </div>
                        </c:when>
                        <c:when test="${canGrade}">
                            <div class="alert alert-primary py-2 px-3 small d-flex align-items-center gap-2 mb-3">
                                <i class="bi bi-pencil-fill text-primary flex-shrink-0"></i>
                                <div>
                                    <strong>Faculty Grading Control:</strong> Enter and update course marks (0–100) for student assessment. Evaluated marks can be saved for review.
                                </div>
                            </div>
                        </c:when>
                        <c:when test="${canPublish}">
                            <div class="alert alert-warning py-2 px-3 small d-flex align-items-center gap-2 mb-3">
                                <i class="bi bi-send-check-fill text-warning flex-shrink-0"></i>
                                <div>
                                    <strong>Supervisor Oversight:</strong> Review evaluated course marks and publish official student results.
                                </div>
                            </div>
                        </c:when>
                        <c:when test="${isStaff}">
                            <div class="alert alert-info py-2 px-3 small d-flex align-items-center gap-2 mb-3">
                                <i class="bi bi-eye-fill text-info flex-shrink-0"></i>
                                <div>
                                    <strong>${userRoleName} View:</strong> Read-only view of course marks, enrollment, and official results.
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-secondary py-2 px-3 small d-flex align-items-center gap-2 mb-3">
                                <i class="bi bi-shield-lock-fill text-secondary flex-shrink-0"></i>
                                <div>
                                    <strong>Student Read-Only Transcript:</strong> Showing official published grades. Only evaluated courses with verified results are computed in your weighted CGPA.
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <!-- Filter Toolbar & Search -->
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                        <div class="btn-group btn-group-sm" role="group" id="resultsFilterGroup">
                            <button type="button" class="btn btn-outline-primary active" data-filter="all">
                                All Courses (<span id="countFilterAll">0</span>)
                            </button>
                            <button type="button" class="btn btn-outline-success" data-filter="evaluated">
                                Evaluated (<span id="countFilterEvaluated">0</span>)
                            </button>
                            <button type="button" class="btn btn-outline-secondary" data-filter="unevaluated">
                                Not Evaluated (<span id="countFilterEmpty">0</span>)
                            </button>
                        </div>
                        <div class="d-flex gap-2 align-items-center flex-wrap">
                            <div class="d-flex align-items-center gap-1 bg-white px-2 py-1 border rounded shadow-sm">
                                <label for="modalCourseSemesterFilter" class="form-label mb-0 text-muted small fw-semibold text-nowrap">
                                    <i class="bi bi-calendar3 text-primary me-1"></i>Semester:
                                </label>
                                <select id="modalCourseSemesterFilter" class="form-select form-select-sm border-0 fw-bold text-dark py-0" style="min-width: 145px; cursor: pointer;">
                                    <option value="ALL">All Semesters</option>
                                    <c:forEach var="sem" items="${availableSemesters}">
                                        <option value="${sem}">${sem}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <input type="text" id="modalCourseSearch" class="form-control form-control-sm" style="max-width: 190px;" placeholder="Search course code/title...">
                        </div>
                    </div>

                    <!-- Loading Spinner -->
                    <div id="resultsLoading" class="text-center py-5">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Loading courses...</span>
                        </div>
                        <p class="mt-2 text-muted small">Fetching curriculum courses &amp; marks...</p>
                    </div>

                    <!-- No Results Notice (For Students when unpublished) -->
                    <div id="noResultsAlert" class="alert alert-warning text-center py-4 d-none" role="alert">
                        <i class="bi bi-clock-history fs-2 text-warning d-block mb-2"></i>
                        <h6 class="fw-bold mb-1">Results Not Published Yet</h6>
                        <p class="text-muted mb-0 small">The administration has not yet published the official results for your courses. Please check back later.</p>
                    </div>

                    <!-- Courses Table Container -->
                    <div id="resultsTableContainer" class="table-responsive d-none">
                        <table class="table table-hover align-middle mb-0" id="resultsCoursesTable">
                            <thead class="table-light">
                                <tr>
                                    <th style="width: 45px;">#</th>
                                    <th style="width: 130px;">Course Code</th>
                                    <th>Course Name &amp; Semester</th>
                                    <th style="width: 80px;" class="text-center">Credits</th>
                                    <th style="width: 140px;" class="text-center">Marks (0-100)</th>
                                    <th style="width: 100px;" class="text-center">Grade Point</th>
                                    <th style="width: 110px;" class="text-center">Letter Grade</th>
                                    <th style="width: 115px;" class="text-center">Status</th>
                                    <c:if test="${canPublish || canGrade}">
                                        <th style="width: 115px;" class="text-center">Action</th>
                                    </c:if>
                                </tr>
                            </thead>
                            <tbody id="resultsTableBody">
                                <!-- Populated dynamically -->
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="modal-footer bg-light border-top d-flex justify-content-between align-items-center py-3">
                    <div class="text-muted small">
                        <span id="footerTotalCourses">0</span> Total Courses | 
                        <span id="footerEvaluatedCourses" class="fw-bold text-dark">0</span> Evaluated | 
                        <span id="footerTotalCredits">0.0</span> Evaluated Credits | 
                        Weighted CGPA: <strong class="text-primary fs-6" id="footerCgpa">0.00</strong>
                    </div>
                    <div class="d-flex align-items-center gap-2 flex-wrap">
                        <button type="button" class="btn btn-secondary px-3" data-bs-dismiss="modal">Close</button>
                        <c:if test="${canGrade}">
                            <button type="button" id="btnSaveResults" class="btn btn-outline-primary px-3 shadow-sm" title="Save entered marks without publishing">
                                <i class="bi bi-save me-1"></i>Save Marks
                            </button>
                        </c:if>
                        <c:if test="${canPublish}">
                            <button type="button" id="btnPublishResults" class="btn btn-success px-4 shadow-sm" title="Publish evaluated courses">
                                <i class="bi bi-send-check-fill me-1"></i>Publish Results
                            </button>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <c:if test="${canAssign}">
        <!-- Course Assignment for New Semester Modal -->
        <div class="modal fade" id="assignCoursesModal" tabindex="-1" aria-labelledby="assignCoursesModalLabel" aria-hidden="true" data-bs-backdrop="static">
            <div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
                <div class="modal-content rounded-4 border-0 shadow">
                    <div class="modal-header bg-light border-bottom py-3">
                        <div class="d-flex align-items-center gap-2">
                            <div class="stat-icon bg-primary bg-opacity-10 text-primary p-2 rounded-circle" style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center;">
                                <i class="bi bi-journal-plus fs-5"></i>
                            </div>
                            <div>
                                <h5 class="modal-title fw-bold mb-0" id="assignCoursesModalLabel">Assign Courses for New Semester</h5>
                                <span class="text-muted small">Tri-Semester System (4 Months duration per semester, 3 semesters in a year)</span>
                            </div>
                        </div>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body p-4">
                        <div class="alert alert-info py-2 px-3 small d-flex align-items-center gap-2 mb-3">
                            <i class="bi bi-calendar3-range text-info fs-5 flex-shrink-0"></i>
                            <div>
                                <strong>Academic Calendar:</strong> Semesters are <strong>4 months each</strong> (Spring: Jan–Apr, Summer: May–Aug, Fall: Sep–Dec). Assign curriculum courses for the upcoming semester to all students in a department or a specific student.
                            </div>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label for="assignDeptSelect" class="form-label fw-semibold small text-muted">Department</label>
                                <select id="assignDeptSelect" class="form-select">
                                    <option value="CSE">CSE - Computer Science &amp; Engineering</option>
                                    <option value="BBA">BBA - Business Administration</option>
                                    <option value="LAW">LAW - Department of Law</option>
                                    <option value="PHARMACY">PHARMACY - Department of Pharmacy</option>
                                    <option value="ENGLISH">ENGLISH - Department of English</option>
                                    <option value="CHEMISTRY">CHEMISTRY - Department of Chemistry</option>
                                    <option value="DATA SCIENCE">DATA SCIENCE - Data Science</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label for="assignSemesterSelect" class="form-label fw-semibold small text-muted">Semester (4 Months Duration)</label>
                                <select id="assignSemesterSelect" class="form-select">
                                    <c:forEach var="sem" items="${availableSemesters}">
                                        <option value="${sem}">${sem}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-semibold small text-muted d-block">Assignment Target</label>
                            <div class="d-flex gap-4">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="assignTargetRadio" id="targetDeptAll" value="ALL" checked>
                                    <label class="form-check-label fw-medium text-dark" for="targetDeptAll">
                                        All Students in Selected Department
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="assignTargetRadio" id="targetDeptSpecific" value="SPECIFIC">
                                    <label class="form-check-label fw-medium text-dark" for="targetDeptSpecific">
                                        Specific Student
                                    </label>
                                </div>
                            </div>
                        </div>

                        <div id="assignSpecificStudentWrapper" class="mb-3 d-none">
                            <label for="assignStudentSelect" class="form-label fw-semibold small text-muted">Select Student</label>
                            <select id="assignStudentSelect" class="form-select">
                                <!-- Populated dynamically based on department -->
                            </select>
                        </div>

                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <div>
                                <span class="fw-semibold small text-muted">Curriculum Courses to Assign</span>
                                <span class="ms-2 small text-primary fw-medium" id="assignSelectedSummary">0 courses selected (0.0 credits)</span>
                            </div>
                            <div class="btn-group btn-group-sm">
                                <button type="button" id="btnAssignSelectAll" class="btn btn-outline-secondary">Select All</button>
                                <button type="button" id="btnAssignDeselectAll" class="btn btn-outline-secondary">Deselect All</button>
                            </div>
                        </div>

                        <!-- Course Checklist -->
                        <div class="course-selection-card" id="assignCoursesListContainer">
                            <div class="text-center py-4 text-muted small">Loading courses for department...</div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-top d-flex justify-content-between align-items-center py-3">
                        <span class="text-muted small">Assigned courses will be registered for the selected 4-month semester.</span>
                        <div class="d-flex gap-2">
                            <button type="button" class="btn btn-secondary px-3" data-bs-dismiss="modal">Cancel</button>
                            <button type="button" id="btnSubmitAssignCourses" class="btn btn-primary px-4 shadow-sm">
                                <i class="bi bi-check2-circle me-1"></i>Assign Courses
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <c:if test="${!isStaff}">
        <!-- Student Course Registration for Upcoming Semester Modal -->
        <div class="modal fade" id="studentRegisterModal" tabindex="-1" aria-labelledby="studentRegisterModalLabel" aria-hidden="true" data-bs-backdrop="static">
            <div class="modal-dialog modal-lg modal-dialog-centered modal-dialog-scrollable">
                <div class="modal-content rounded-4 border-0 shadow">
                    <div class="modal-header bg-light border-bottom py-3">
                        <div class="d-flex align-items-center gap-2">
                            <div class="stat-icon bg-success bg-opacity-10 text-success p-2 rounded-circle" style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center;">
                                <i class="bi bi-calendar-check-fill fs-5"></i>
                            </div>
                            <div>
                                <h5 class="modal-title fw-bold mb-0" id="studentRegisterModalLabel">Course Registration for Upcoming Semester</h5>
                                <span class="text-muted small">AIUB Tri-Semester System (4 Months duration per semester, 3 semesters in a year)</span>
                            </div>
                        </div>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body p-4">
                        <div class="alert alert-success py-2 px-3 small d-flex align-items-center gap-2 mb-3">
                            <i class="bi bi-info-circle-fill text-success fs-5 flex-shrink-0"></i>
                            <div>
                                <strong>Upcoming Semester:</strong> When a semester finishes, register your courses for the upcoming 4-month semester: 
                                <span class="badge bg-success fs-7 ms-1" id="studentRegisterUpcomingSemName">-</span>
                                <span class="text-muted ms-2 small" id="studentRegisterUpcomingSemDuration">(4 Months)</span>
                            </div>
                        </div>

                        <div class="card bg-body-tertiary border-0 rounded-3 p-3 mb-3">
                            <div class="row g-2 align-items-center">
                                <div class="col-sm-4">
                                    <small class="text-muted d-block">Department</small>
                                    <span class="fw-bold text-dark" id="studentRegDeptDisplay">-</span>
                                </div>
                                <div class="col-sm-4">
                                    <small class="text-muted d-block">Selected Credits</small>
                                    <span class="fs-5 fw-bold text-primary" id="studentRegTotalCredits">0.0</span>
                                    <span class="text-muted small"> Credits (Recommended 9–15)</span>
                                </div>
                                <div class="col-sm-4 text-sm-end">
                                    <small class="text-muted d-block">Selected Courses</small>
                                    <span class="fs-5 fw-bold text-dark" id="studentRegTotalCount">0</span> Courses
                                </div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="fw-semibold small text-muted">Select Courses for Registration:</span>
                            <div class="btn-group btn-group-sm">
                                <button type="button" id="btnStudentRegSelectAll" class="btn btn-outline-secondary">Select All</button>
                                <button type="button" id="btnStudentRegDeselectAll" class="btn btn-outline-secondary">Deselect All</button>
                            </div>
                        </div>

                        <!-- Course Checklist -->
                        <div class="course-selection-card" id="studentRegisterCoursesContainer">
                            <div class="text-center py-4 text-muted small">Loading department courses...</div>
                        </div>
                    </div>
                    <div class="modal-footer bg-light border-top d-flex justify-content-between align-items-center py-3">
                        <span class="text-muted small">Courses will be registered for the upcoming 4-month academic term.</span>
                        <div class="d-flex gap-2">
                            <button type="button" class="btn btn-secondary px-3" data-bs-dismiss="modal">Cancel</button>
                            <button type="button" id="btnSubmitStudentRegister" class="btn btn-success px-4 shadow-sm">
                                <i class="bi bi-calendar-plus me-1"></i>Register Courses
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <%@ include file="/WEB-INF/fragments/footer.jsp" %>
    <%@ include file="/WEB-INF/fragments/scripts.jsp" %>

    <script>
        $(document).ready(function () {
            const contextPath = '${pageContext.request.contextPath}';
            const permissions = {
                isStaff: ${isStaff ? 'true' : 'false'},
                isAdmin: ${isAdmin ? 'true' : 'false'},
                canAdd: ${canAdd ? 'true' : 'false'},
                canEdit: ${canEdit ? 'true' : 'false'},
                canDelete: ${canDelete ? 'true' : 'false'},
                canAssign: ${canAssign ? 'true' : 'false'},
                canGrade: ${canGrade ? 'true' : 'false'},
                canPublish: ${canPublish ? 'true' : 'false'},
                role: '${sessionScope.user != null ? sessionScope.user.roleCode : "STUDENT"}',
                roleName: '${userRoleName}'
            };
            const isStaff = permissions.isStaff;
            const isAdmin = permissions.isAdmin;
            const currentStudentId = '<c:if test="${!isStaff && not empty students}">${students[0].id}</c:if>';
            const currentStudentUid = '<c:if test="${!isStaff && not empty students}">${students[0].uid}</c:if>';
            const currentStudentDept = '<c:if test="${!isStaff && not empty students}">${students[0].department}</c:if>';
            const currentStudentName = '<c:if test="${!isStaff && not empty students}">${students[0].name}</c:if>';

            const availableSemestersList = [
                <c:forEach var="s" items="${availableSemesters}" varStatus="loop">
                    "${s}"${!loop.last ? ',' : ''}
                </c:forEach>
            ];
            if (!availableSemestersList || availableSemestersList.length === 0) {
                ['SPRING 2024', 'SUMMER 2024', 'FALL 2024', 'SPRING 2025', 'SUMMER 2025', 'FALL 2025', 'SPRING 2026', 'SUMMER 2026', 'FALL 2026', 'SPRING 2027', 'SUMMER 2027', 'FALL 2027'].forEach(s => availableSemestersList.push(s));
            }

            function getSemesterDurationText(sem) {
                if (!sem) return '4 Months Semester';
                const upper = sem.toString().toUpperCase();
                if (upper.indexOf('SPRING') !== -1) return '4 Months (January - April)';
                if (upper.indexOf('SUMMER') !== -1) return '4 Months (May - August)';
                if (upper.indexOf('FALL') !== -1) return '4 Months (September - December)';
                return '4 Months Semester';
            }

            let studentToDeleteId = null;
            let studentToDeleteUid = null;
            let currentResultsStudentId = null;
            let currentResultsStudentUid = null;
            let currentResultsList = [];
            let activeResultsFilter = 'all';

            const modalEl = document.getElementById('deleteConfirmModal');
            const deleteModal = modalEl ? new bootstrap.Modal(modalEl) : null;
            const resultsModalEl = document.getElementById('resultsModal');
            const resultsModal = resultsModalEl ? new bootstrap.Modal(resultsModalEl) : null;
            const assignCoursesModalEl = document.getElementById('assignCoursesModal');
            const assignCoursesModal = assignCoursesModalEl ? new bootstrap.Modal(assignCoursesModalEl) : null;
            const studentRegisterModalEl = document.getElementById('studentRegisterModal');
            const studentRegisterModal = studentRegisterModalEl ? new bootstrap.Modal(studentRegisterModalEl) : null;
            const liveToast = new bootstrap.Toast(document.getElementById('liveToast'), { delay: 4000 });

            function showToast(type, message) {
                const $toast = $('#liveToast');
                const $icon = $('#toastIcon');
                const $msg = $('#toastMessage');

                $toast.removeClass('border-success border-danger text-success text-danger');
                $icon.removeClass('bi-check-circle-fill bi-exclamation-circle-fill text-success text-danger');

                if (type === 'success') {
                    $toast.addClass('border-success');
                    $icon.addClass('bi-check-circle-fill text-success');
                } else {
                    $toast.addClass('border-danger');
                    $icon.addClass('bi-check-circle-fill text-danger');
                }
                $msg.text(message);
                liveToast.show();
            }

            function escapeHtml(text) {
                return text ? $('<div>').text(text).html() : '';
            }

            function getGradePoint(marks) {
                if (marks === null || marks === undefined || marks === '' || isNaN(marks)) return 0.00;
                const m = parseFloat(marks);
                if (m >= 90) return 4.00;
                if (m >= 85) return 3.75;
                if (m >= 80) return 3.50;
                if (m >= 75) return 3.25;
                if (m >= 70) return 3.00;
                if (m >= 65) return 2.75;
                if (m >= 60) return 2.50;
                if (m >= 50) return 2.25;
                return 0.00;
            }

            function getLetterGrade(marks) {
                if (marks === null || marks === undefined || marks === '' || isNaN(marks)) return '-';
                const m = parseFloat(marks);
                if (m >= 90) return 'A+';
                if (m >= 85) return 'A';
                if (m >= 80) return 'B+';
                if (m >= 75) return 'B';
                if (m >= 70) return 'C+';
                if (m >= 65) return 'C';
                if (m >= 60) return 'D+';
                if (m >= 50) return 'D';
                return 'F';
            }

            function getGradeBadgeClass(letter) {
                switch (letter) {
                    case 'A+': return 'badge-grade badge-grade-ap';
                    case 'A': return 'badge-grade badge-grade-a';
                    case 'B+': return 'badge-grade badge-grade-bp';
                    case 'B': return 'badge-grade badge-grade-b';
                    case 'C+': return 'badge-grade badge-grade-cp';
                    case 'C': return 'badge-grade badge-grade-c';
                    case 'D+': return 'badge-grade badge-grade-dp';
                    case 'D': return 'badge-grade badge-grade-d';
                    case 'F': return 'badge-grade badge-grade-f';
                    default: return 'badge bg-secondary';
                }
            }

            function computeLiveCgpa() {
                let totalCredits = 0;
                let weightedPoints = 0;
                let evaluatedCount = 0;

                $('#resultsTableBody tr').each(function () {
                    const isEvaluated = ($(this).attr('data-evaluated') === 'true');
                    const credits = parseFloat($(this).data('credits')) || 3.0;
                    let marks = NaN;

                    if (permissions.canGrade) {
                        const val = $(this).find('.course-marks-input').val();
                        if (val !== undefined && val !== '') {
                            marks = parseFloat(val);
                        }
                    } else {
                        const val = $(this).attr('data-marks');
                        if (val !== undefined && val !== '') {
                            marks = parseFloat(val);
                        }
                    }

                    // Only count genuine evaluated courses with entered marks
                    if (isEvaluated && !isNaN(marks) && marks >= 0) {
                        const gp = getGradePoint(marks);
                        weightedPoints += (gp * credits);
                        totalCredits += credits;
                        evaluatedCount++;
                    }
                });

                const cgpa = totalCredits > 0 ? (weightedPoints / totalCredits) : 0.00;
                const cgpaFormatted = cgpa.toFixed(2);
                $('#modalCgpaDisplay').text(cgpaFormatted);
                $('#footerCgpa').text(cgpaFormatted);
                $('#footerTotalCredits').text(totalCredits.toFixed(1));
                $('#footerEvaluatedCourses').text(evaluatedCount);
                $('#modalEvaluatedCount').text(evaluatedCount + ' Evaluated Courses (' + totalCredits.toFixed(1) + ' Credits)');
                return cgpaFormatted;
            }

            function updateStatsAndFilters(students) {
                if (!students) return;

                const filterDepts = new Set(['BBA', 'CSE', 'LAW', 'PHARMACY', 'ENGLISH', 'CHEMISTRY', 'DATA SCIENCE']);
                const studentDepts = new Set();
                const filterSemesters = new Set();

                const curYear = new Date().getFullYear();
                ['SPRING', 'SUMMER', 'FALL'].forEach(function(term) {
                    for (let yr = curYear - 1; yr <= curYear + 1; yr++) {
                        filterSemesters.add(term + ' ' + yr);
                    }
                });

                if (isStaff) {
                    const total = students.length;
                    let sum = 0;
                    let highest = 0;

                    students.forEach(s => {
                        const cgpa = parseFloat(s.cgpa) || 0;
                        sum += cgpa;
                        if (cgpa > highest) highest = cgpa;
                        if (s.department && s.department.trim()) {
                            const dept = s.department.trim().toUpperCase();
                            studentDepts.add(dept);
                            filterDepts.add(dept);
                        }
                        if (s.semester && s.semester.trim()) {
                            filterSemesters.add(s.semester.trim().toUpperCase());
                        }
                    });

                    $('#statTotal').text(total);
                    $('#statAvgCgpa').text(total > 0 ? (sum / total).toFixed(2) : '0.00');
                    $('#statHighestCgpa').text(highest > 0 ? highest.toFixed(2) : '0.00');
                    $('#statDeptCount').text(studentDepts.size);
                } else {
                    students.forEach(s => {
                        if (s.department && s.department.trim()) {
                            filterDepts.add(s.department.trim().toUpperCase());
                        }
                        if (s.semester && s.semester.trim()) {
                            filterSemesters.add(s.semester.trim().toUpperCase());
                        }
                    });
                }

                const currentDept = $('#deptFilter').val();
                let deptOptionsHtml = '<option value="ALL">All Departments</option>';
                filterDepts.forEach(d => { deptOptionsHtml += '<option value="' + d + '">' + d + '</option>'; });
                $('#deptFilter').html(deptOptionsHtml);
                if (currentDept && filterDepts.has(currentDept)) {
                    $('#deptFilter').val(currentDept);
                }

                const currentSem = $('#semesterFilter').val();
                let semOptionsHtml = '<option value="ALL">All Semesters</option>';
                Array.from(filterSemesters).forEach(sm => {
                    semOptionsHtml += '<option value="' + sm + '">' + sm + '</option>';
                });
                $('#semesterFilter').html(semOptionsHtml);
                if (currentSem && (currentSem === 'ALL' || filterSemesters.has(currentSem))) {
                    $('#semesterFilter').val(currentSem);
                }
            }

            function loadStudents() {
                const $icon = $('#refreshIcon');
                $icon.addClass('spinner-border spinner-border-sm').removeClass('bi bi-arrow-clockwise');

                $.ajax({
                    url: contextPath + '/api/students',
                    type: 'POST',
                    contentType: 'application/json; charset=UTF-8',
                    data: JSON.stringify({ action: 'list' }),
                    headers: { 'X-Requested-With': 'XMLHttpRequest' },
                    success: function (response) {
                        if (response.success && response.students) {
                            renderTable(response.students);
                            updateStatsAndFilters(response.students);
                            showToast('success', 'Student list updated successfully (' + response.students.length + ' records).');
                        } else {
                            showToast('danger', response.message || 'Unable to retrieve students.');
                        }
                    },
                    error: function () {
                        showToast('danger', 'API Error: Failed to fetch student data.');
                    },
                    complete: function () {
                        $icon.removeClass('spinner-border spinner-border-sm').addClass('bi bi-arrow-clockwise');
                    }
                });
            }

            function renderTable(students) {
                const $tbody = $('#studentTableBody');
                $tbody.empty();
                const colSpan = isStaff ? 9 : 8;

                if (!students || students.length === 0) {
                    const emptyAddBtn = permissions.canAdd ? '<div class="mt-2"><a href="' + contextPath + '/add-student" class="btn btn-sm btn-outline-primary">Add the first student</a></div>' : '';
                    $tbody.html('<tr id="emptyStateRow"><td colspan="' + colSpan + '" class="text-center py-5 text-secondary"><i class="bi bi-inbox fs-1 d-block mb-2 text-muted"></i><span>No student records found.</span>' + emptyAddBtn + '</td></tr>');
                    return;
                }

                students.forEach(student => {
                    let actionCol = '';
                    if (isStaff) {
                        let btnGroup = '<button type="button" class="btn btn-sm btn-outline-success btn-results" data-id="' + escapeHtml(student.id) + '" data-uid="' + escapeHtml(student.uid) + '" data-name="' + escapeHtml(student.name) + '" data-dept="' + escapeHtml(student.department) + '" data-semester="' + escapeHtml(student.semester || '') + '" title="View &amp; Manage Results"><i class="bi bi-mortarboard-fill me-1"></i>Results</button>';
                        if (permissions.canEdit) {
                            btnGroup += '<a href="' + contextPath + '/edit-student?uid=' + encodeURIComponent(student.uid) + '" class="btn btn-sm btn-outline-primary" title="Edit Student"><i class="bi bi-pencil-square me-1"></i>Edit</a>';
                        }
                        if (permissions.canDelete) {
                            btnGroup += '<button type="button" class="btn btn-sm btn-outline-danger btn-delete" data-id="' + escapeHtml(student.id) + '" data-uid="' + escapeHtml(student.uid) + '" data-name="' + escapeHtml(student.name) + '" title="Delete Student"><i class="bi bi-trash3 me-1"></i>Delete</button>';
                        }
                        actionCol = '<td><span class="badge bg-secondary font-monospace px-2 py-1">' + escapeHtml(student.username || '-') + '</span></td>' +
                            '<td class="text-end pe-4"><div class="btn-group" role="group">' + btnGroup + '</div></td>';
                    } else {
                        actionCol = '<td class="text-end pe-4">' +
                            '<button type="button" class="btn btn-sm btn-outline-primary btn-results" data-id="' + escapeHtml(student.id) + '" data-uid="' + escapeHtml(student.uid) + '" data-name="' + escapeHtml(student.name) + '" data-dept="' + escapeHtml(student.department) + '" data-semester="' + escapeHtml(student.semester || '') + '" title="View Academic Results">' +
                            '<i class="bi bi-file-earmark-text me-1"></i>View Grades</button></td>';
                    }

                    $tbody.append(
                        '<tr id="student-row-' + escapeHtml(student.id) + '" class="student-item" data-id="' + escapeHtml(student.id) + '" data-uid="' + escapeHtml(student.uid) + '" data-name="' + escapeHtml(student.name) + '" data-username="' + escapeHtml(student.username || '') + '" data-email="' + escapeHtml(student.email) + '" data-phone="' + escapeHtml(student.phone) + '" data-dept="' + escapeHtml(student.department) + '" data-semester="' + escapeHtml(student.semester || '') + '">' +
                        '<td class="ps-4 fw-bold text-primary font-monospace">#' + escapeHtml(student.id) + '</td>' +
                        '<td class="fw-semibold text-dark student-name">' + escapeHtml(student.name) + '</td>' +
                        '<td><a href="mailto:' + escapeHtml(student.email) + '" class="text-decoration-none text-muted"><i class="bi bi-envelope me-1"></i>' + escapeHtml(student.email) + '</a></td>' +
                        '<td><span class="text-muted"><i class="bi bi-telephone me-1"></i>' + escapeHtml(student.phone) + '</span></td>' +
                        '<td><span class="badge-dept">' + escapeHtml(student.department) + '</span></td>' +
                        '<td><span class="badge-semester">' + escapeHtml(student.semester || '-') + '</span></td>' +
                        '<td><span class="badge-cgpa">' + parseFloat(student.cgpa).toFixed(2) + '</span></td>' +
                        actionCol +
                        '</tr>'
                    );
                });

                applyFilters();
            }

            function applyFilters() {
                const query = ($('#searchInput').val() || '').toLowerCase().trim();
                const selectedDept = $('#deptFilter').val() || 'ALL';
                const selectedSemester = $('#semesterFilter').val() || 'ALL';
                let visibleCount = 0;

                $('.student-item').each(function () {
                    const id = ($(this).data('id') || '').toString();
                    const uid = ($(this).data('uid') || '').toString().toLowerCase();
                    const name = ($(this).data('name') || '').toString().toLowerCase();
                    const username = ($(this).data('username') || '').toString().toLowerCase();
                    const email = ($(this).data('email') || '').toString().toLowerCase();
                    const phone = ($(this).data('phone') || '').toString().toLowerCase();
                    const dept = ($(this).data('dept') || '').toString().toUpperCase();
                    const sem = ($(this).data('semester') || '').toString().toUpperCase();

                    const matchesSearch = !query || id.includes(query) || ('#' + id).includes(query) || uid.includes(query) || name.includes(query) || username.includes(query) || email.includes(query) || phone.includes(query) || dept.toLowerCase().includes(query) || sem.toLowerCase().includes(query);
                    const matchesDept = (selectedDept === 'ALL') || (dept === selectedDept);
                    const matchesSemester = (selectedSemester === 'ALL') || (sem === selectedSemester.toUpperCase());

                    if (matchesSearch && matchesDept && matchesSemester) {
                        $(this).show();
                        visibleCount++;
                    } else {
                        $(this).hide();
                    }
                });

                const colSpan = isStaff ? 9 : 8;
                if (visibleCount === 0 && $('.student-item').length > 0) {
                    if ($('#noMatchRow').length === 0) {
                        $('#studentTableBody').append('<tr id="noMatchRow"><td colspan="' + colSpan + '" class="text-center py-4 text-muted"><i class="bi bi-search fs-3 d-block mb-1"></i>No students matching your filter criteria.</td></tr>');
                    }
                } else {
                    $('#noMatchRow').remove();
                }
            }

            $('#searchInput').on('input keyup', applyFilters);
            $('#deptFilter').on('change', applyFilters);
            $('#semesterFilter').on('change', applyFilters);

            $(document).on('click', '.btn-results', function (e) {
                e.preventDefault();
                const id = $(this).data('id');
                const uid = $(this).data('uid');
                const name = $(this).data('name');
                const dept = $(this).data('dept');
                const semester = $(this).data('semester');
                openResultsModal(id, uid, name, dept, semester);
            });

            function reloadResultsModal() {
                if (!currentResultsStudentUid && !currentResultsStudentId) return;
                openResultsModal(
                    currentResultsStudentId,
                    currentResultsStudentUid,
                    $('#modalStudentName').text(),
                    $('#modalStudentDept').text(),
                    $('#modalStudentSem').text()
                );
            }

            function openResultsModal(id, uid, fallbackName, fallbackDept, fallbackSem) {
                if (!uid && typeof id === 'string' && id.includes('-')) {
                    uid = id;
                    id = null;
                }
                if (id !== undefined && id !== null && id !== '') {
                    currentResultsStudentId = id;
                }
                if (uid !== undefined && uid !== null && uid !== '') {
                    currentResultsStudentUid = uid;
                }

                const requestId = currentResultsStudentId;
                const requestUid = currentResultsStudentUid;

                $('#modalStudentName').text(fallbackName || '-');
                $('#modalStudentUid').text(requestId ? ('Student ID: #' + requestId) : (requestUid ? ('ID: ' + requestUid) : '-'));
                $('#modalStudentDept').text(fallbackDept || '-');
                const initialSem = (fallbackSem || 'FALL 2025').trim();
                $('#modalStudentSem').text(initialSem);
                $('#modalSemDuration').html('<i class="bi bi-clock-history me-1"></i>' + getSemesterDurationText(initialSem));
                $('#modalCgpaDisplay').text('0.00');
                $('#footerCgpa').text('0.00');
                $('#modalPublishStatusBadge').removeClass('bg-success bg-warning bg-secondary bg-info text-white text-dark')
                    .addClass('bg-secondary text-white').text('Loading...');

                $('#resultsLoading').removeClass('d-none');
                $('#resultsTableContainer').addClass('d-none');
                $('#noResultsAlert').addClass('d-none');

                if (resultsModal) resultsModal.show();

                $.ajax({
                    url: contextPath + '/api/students',
                    type: 'POST',
                    contentType: 'application/json; charset=UTF-8',
                    data: JSON.stringify({ action: 'get_results', id: requestId, uid: requestUid }),
                    headers: { 'X-Requested-With': 'XMLHttpRequest' },
                    success: function (res) {
                        $('#resultsLoading').addClass('d-none');
                        if (res.success) {
                            let stuSem = initialSem;
                            if (res.student) {
                                $('#modalStudentName').text(res.student.name || fallbackName);
                                $('#modalStudentUid').text(res.student.id ? ('Student ID: #' + res.student.id) : (res.student.uid ? ('ID: ' + res.student.uid) : '-'));
                                $('#modalStudentDept').text(res.student.department || fallbackDept);
                                stuSem = (res.student.semester || fallbackSem || 'FALL 2025').trim();
                                $('#modalStudentSem').text(stuSem);
                            }

                            if (res.currentSemesterDuration) {
                                $('#modalSemDuration').html('<i class="bi bi-clock-history me-1"></i>' + escapeHtml(res.currentSemesterDuration));
                            } else {
                                $('#modalSemDuration').html('<i class="bi bi-clock-history me-1"></i>' + getSemesterDurationText(stuSem));
                            }

                            const resultsList = res.results || [];
                            currentResultsList = resultsList;
                            const canGrade = (res.canEdit !== undefined ? res.canEdit : permissions.canGrade) && permissions.canGrade;
                            const canPublish = (res.canPublish !== undefined ? res.canPublish : permissions.canPublish) && permissions.canPublish;

                            if (!resultsList || resultsList.length === 0) {
                                if (!isStaff) {
                                    $('#noResultsAlert').removeClass('d-none');
                                    $('#modalPublishStatusBadge').removeClass('bg-secondary bg-success text-white').addClass('bg-warning text-dark').text('Not Published');
                                } else {
                                    $('#resultsTableContainer').removeClass('d-none');
                                    $('#resultsTableBody').html('<tr><td colspan="' + ((canPublish || canGrade) ? 9 : 8) + '" class="text-center py-4 text-muted">No official curriculum courses found for department ' + escapeHtml(res.student ? res.student.department : '') + '</td></tr>');
                                }
                                return;
                            }

                            let anyEvaluated = false;
                            let anyPublished = false;
                            let allEvaluatedPublished = true;

                            resultsList.forEach(function (item) {
                                if (item.evaluated) {
                                    anyEvaluated = true;
                                    if (item.published) {
                                        anyPublished = true;
                                    } else {
                                        allEvaluatedPublished = false;
                                    }
                                }
                            });

                            if (anyEvaluated && allEvaluatedPublished) {
                                $('#modalPublishStatusBadge').removeClass('bg-secondary bg-warning bg-info text-dark').addClass('bg-success text-white').html('<i class="bi bi-check-circle-fill me-1"></i>Published');
                            } else if (anyPublished) {
                                $('#modalPublishStatusBadge').removeClass('bg-secondary bg-success text-white bg-warning text-dark').addClass('bg-info text-white').html('<i class="bi bi-clock-history me-1"></i>Partially Published');
                            } else if (anyEvaluated) {
                                $('#modalPublishStatusBadge').removeClass('bg-secondary bg-success text-white bg-info').addClass('bg-warning text-dark').html('<i class="bi bi-exclamation-circle me-1"></i>Unpublished');
                            } else {
                                $('#modalPublishStatusBadge').removeClass('bg-success bg-info bg-warning text-white text-dark').addClass('bg-secondary text-white').html('<i class="bi bi-dash-circle me-1"></i>Not Evaluated');
                            }

                            // Build options for modal semester filter combo boxes
                            let semFilterOpts = '<option value="ALL">All Semesters</option>';
                            const studentFoundSemesters = new Set();
                            resultsList.forEach(function (item) {
                                if (item.semester && item.semester.trim()) {
                                    studentFoundSemesters.add(item.semester.trim().toUpperCase());
                                }
                            });
                            studentFoundSemesters.forEach(function (s) {
                                semFilterOpts += '<option value="' + s + '">' + s + '</option>';
                            });
                            availableSemestersList.forEach(function (s) {
                                if (!studentFoundSemesters.has(s.toUpperCase())) {
                                    semFilterOpts += '<option value="' + s + '">' + s + '</option>';
                                }
                            });
                            $('#modalCourseSemesterFilter').html(semFilterOpts).val('ALL');

                            renderResultsTable(resultsList, canGrade, canPublish);
                            $('#resultsTableContainer').removeClass('d-none');
                        } else {
                            showToast('danger', res.message || 'Failed to load student results.');
                            if (resultsModal) resultsModal.hide();
                        }
                    },
                    error: function (xhr) {
                        $('#resultsLoading').addClass('d-none');
                        let errMsg = 'Error loading course results.';
                        try {
                            const errData = JSON.parse(xhr.responseText);
                            if (errData && errData.message) errMsg = errData.message;
                        } catch (e) {}
                        showToast('danger', errMsg);
                        if (resultsModal) resultsModal.hide();
                    }
                });
            }

            function renderResultsTable(results, canGrade, canPublish) {
                const $tbody = $('#resultsTableBody');
                $tbody.empty();

                let totalCredits = 0;
                let weightedPoints = 0;
                let evaluatedCount = 0;
                let unevaluatedCount = 0;
                const activeOverallSem = $('#modalStudentSem').text() || 'FALL 2025';

                results.forEach(function (r, index) {
                    const credits = parseFloat(r.creditHours) || 3.0;
                    const isEvaluated = (r.evaluated === true);
                    const hasMarks = (isEvaluated && r.marks !== null && r.marks !== undefined && r.marks >= 0);
                    const marks = hasMarks ? parseFloat(r.marks) : '';
                    const gp = hasMarks ? getGradePoint(marks) : 0.00;
                    const letter = hasMarks ? getLetterGrade(marks) : '-';
                    const badgeClass = hasMarks ? getGradeBadgeClass(letter) : 'badge bg-secondary-subtle text-secondary';
                    const isPub = (isEvaluated && r.published === true);
                    const courseSem = (r.semester && r.semester.trim()) ? r.semester.trim() : activeOverallSem;

                    if (isEvaluated && hasMarks) {
                        weightedPoints += (gp * credits);
                        totalCredits += credits;
                        evaluatedCount++;
                    } else {
                        unevaluatedCount++;
                    }

                    let marksHtml = '';
                    if (canGrade) {
                        marksHtml = '<input type="number" min="0" max="100" step="0.5" ' +
                            'class="form-control form-control-sm text-center fw-bold course-marks-input" ' +
                            'data-course-id="' + r.courseId + '" ' +
                            'data-credits="' + credits + '" ' +
                            'value="' + (hasMarks ? marks : '') + '" placeholder="Enter marks">';
                    } else {
                        marksHtml = '<span class="fw-bold fs-6 text-dark">' + (hasMarks ? marks : '<span class="text-muted">-</span>') + '</span>';
                    }

                    let statusBadge = '';
                    if (isPub) {
                        statusBadge = '<span class="badge bg-success-subtle text-success border border-success-subtle"><i class="bi bi-check-circle me-1"></i>Published</span>';
                    } else if (isEvaluated) {
                        statusBadge = '<span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle"><i class="bi bi-clock me-1"></i>Unpublished</span>';
                    } else {
                        statusBadge = '<span class="badge badge-unevaluated"><i class="bi bi-dash-circle me-1"></i>Not Evaluated</span>';
                    }

                    let actionHtml = '';
                    if (canPublish) {
                        if (isEvaluated) {
                            if (isPub) {
                                actionHtml = '<button type="button" class="btn btn-xs btn-outline-warning btn-single-publish" data-course-id="' + r.courseId + '" data-publish="false" title="Unpublish this course"><i class="bi bi-eye-slash me-1"></i>Unpublish</button>';
                            } else {
                                actionHtml = '<button type="button" class="btn btn-xs btn-outline-success btn-single-publish" data-course-id="' + r.courseId + '" data-publish="true" title="Publish this course"><i class="bi bi-send-check me-1"></i>Publish</button>';
                            }
                        } else {
                            actionHtml = '<button type="button" class="btn btn-xs btn-light text-muted border btn-single-publish-disabled" disabled title="Enter marks first to publish"><i class="bi bi-lock me-1"></i>Publish</button>';
                        }
                    } else if (canGrade) {
                        actionHtml = isEvaluated ? '<span class="badge bg-primary-subtle text-primary border border-primary-subtle small">Marks Entered</span>' : '<span class="badge bg-light text-muted border small">Pending</span>';
                    }

                    let semElement = '';
                    if (canGrade) {
                        let semOpts = '';
                        availableSemestersList.forEach(function (s) {
                            semOpts += '<option value="' + s + '" ' + (s === courseSem ? 'selected' : '') + '>' + s + '</option>';
                        });
                        semElement = '<select class="form-select form-select-xs course-row-semester-select d-inline-block border text-secondary fw-semibold font-monospace me-1" style="width: auto; max-width: 140px; font-size: 0.72rem; padding: 0.1rem 0.35rem; height: 24px; vertical-align: middle;" data-course-id="' + r.courseId + '" title="Edit semester for this course">' + semOpts + '</select>';
                    } else {
                        semElement = courseSem ? ('<span class="badge bg-light text-secondary border me-1 small font-monospace">' + escapeHtml(courseSem) + '</span>') : '';
                    }

                    const enrolledInfo = '<div class="d-flex align-items-center flex-wrap gap-1 mt-1 font-monospace" style="font-size:0.75rem;">' +
                        semElement +
                        (r.enrolledBy ? ('<span class="text-muted small">Enrolled by ' + escapeHtml(r.enrolledBy) + '</span>') : '') +
                        '</div>';

                    const row = '<tr id="course-row-' + r.courseId + '" ' +
                        'data-course-id="' + r.courseId + '" ' +
                        'data-credits="' + credits + '" ' +
                        'data-marks="' + (hasMarks ? marks : '') + '" ' +
                        'data-evaluated="' + (isEvaluated ? 'true' : 'false') + '" ' +
                        'data-published="' + (isPub ? 'true' : 'false') + '" ' +
                        'data-semester="' + escapeHtml(courseSem) + '" ' +
                        'data-code="' + escapeHtml(r.courseCode).toLowerCase() + '" ' +
                        'data-name="' + escapeHtml(r.courseName).toLowerCase() + '">' +
                        '<td class="text-muted">' + (index + 1) + '</td>' +
                        '<td><span class="font-monospace fw-semibold text-primary">' + escapeHtml(r.courseCode) + '</span></td>' +
                        '<td><div class="fw-medium text-dark">' + escapeHtml(r.courseName) + '</div>' + enrolledInfo + '</td>' +
                        '<td class="text-center font-monospace">' + credits.toFixed(1) + '</td>' +
                        '<td class="text-center" style="max-width: 140px;">' + marksHtml + '</td>' +
                        '<td class="text-center font-monospace fw-bold grade-point-cell">' + (hasMarks ? gp.toFixed(2) : '<span class="text-muted">-</span>') + '</td>' +
                        '<td class="text-center"><span class="' + badgeClass + ' letter-grade-badge">' + letter + '</span></td>' +
                        '<td class="text-center status-cell">' + statusBadge + '</td>' +
                        ((canPublish || canGrade) ? ('<td class="text-center action-cell">' + actionHtml + '</td>') : '') +
                        '</tr>';
                    $tbody.append(row);
                });

                $('#countFilterAll').text(results.length);
                $('#countFilterEvaluated').text(evaluatedCount);
                $('#countFilterEmpty').text(unevaluatedCount);

                $('#footerTotalCourses').text(results.length);
                $('#footerEvaluatedCourses').text(evaluatedCount);
                $('#footerTotalCredits').text(totalCredits.toFixed(1));

                const calculatedCgpa = totalCredits > 0 ? (weightedPoints / totalCredits).toFixed(2) : '0.00';
                $('#modalCgpaDisplay').text(calculatedCgpa);
                $('#footerCgpa').text(calculatedCgpa);
                $('#modalEvaluatedCount').text(evaluatedCount + ' Evaluated Courses (' + totalCredits.toFixed(1) + ' Credits)');

                filterResultsTableRows();
            }

            function filterResultsTableRows() {
                const search = ($('#modalCourseSearch').val() || '').toLowerCase().trim();
                const semFilter = ($('#modalCourseSemesterFilter').val() || 'ALL').toUpperCase();

                let visibleCount = 0;
                let semCredits = 0;
                let semPoints = 0;
                let semEvalCount = 0;
                let semUnevalCount = 0;
                let semTotalCount = 0;

                $('#resultsTableBody tr').each(function () {
                    if ($(this).attr('id') === 'modalNoSemesterMatchRow') return;

                    const isEvaluated = ($(this).attr('data-evaluated') === 'true');
                    const isPublished = ($(this).attr('data-published') === 'true');
                    const code = $(this).attr('data-code') || '';
                    const name = $(this).attr('data-name') || '';
                    const rowSem = ($(this).attr('data-semester') || '').toString().trim().toUpperCase();
                    const credits = parseFloat($(this).data('credits')) || 3.0;
                    const marks = parseFloat($(this).attr('data-marks'));

                    const matchSem = (semFilter === 'ALL') || (rowSem === semFilter);

                    if (matchSem) {
                        semTotalCount++;
                        if (isEvaluated && !isNaN(marks) && marks >= 0) {
                            semEvalCount++;
                            semCredits += credits;
                            semPoints += (getGradePoint(marks) * credits);
                        } else {
                            semUnevalCount++;
                        }
                    }

                    let matchFilter = true;
                    if (activeResultsFilter === 'evaluated') {
                        matchFilter = isEvaluated;
                    } else if (activeResultsFilter === 'unevaluated') {
                        matchFilter = !isEvaluated;
                    }

                    const matchSearch = !search || code.includes(search) || name.includes(search);

                    if (matchFilter && matchSearch && matchSem) {
                        $(this).show();
                        visibleCount++;
                    } else {
                        $(this).hide();
                    }
                });

                $('#countFilterAll').text(semTotalCount);
                $('#countFilterEvaluated').text(semEvalCount);
                $('#countFilterEmpty').text(semUnevalCount);

                const semCgpa = (semCredits > 0) ? (semPoints / semCredits).toFixed(2) : '0.00';
                $('#modalCgpaDisplay').text(semCgpa);
                $('#footerCgpa').text(semCgpa);
                $('#footerTotalCourses').text(semTotalCount);
                $('#footerEvaluatedCourses').text(semEvalCount);
                $('#footerTotalCredits').text(semCredits.toFixed(1));

                if (semFilter !== 'ALL') {
                    $('#modalEvaluatedCount').text(semEvalCount + ' Course' + (semEvalCount === 1 ? '' : 's') + ' in ' + semFilter + ' (' + semCredits.toFixed(1) + ' Credits)');
                    $('#modalSemDuration').html('<i class="bi bi-clock-history me-1"></i>' + getSemesterDurationText(semFilter));
                } else {
                    $('#modalEvaluatedCount').text(semEvalCount + ' Evaluated Courses (' + semCredits.toFixed(1) + ' Credits)');
                    const currentActiveSem = $('#modalStudentSem').text() || 'FALL 2025';
                    $('#modalSemDuration').html('<i class="bi bi-clock-history me-1"></i>' + getSemesterDurationText(currentActiveSem));
                }

                $('#modalNoSemesterMatchRow').remove();
                if (visibleCount === 0 && $('#resultsTableBody tr').length > 0) {
                    const colSpan = (permissions.canPublish || permissions.canGrade) ? 9 : 8;
                    const msg = (semFilter !== 'ALL') 
                        ? ('No courses or grades found for semester ' + escapeHtml(semFilter) + '.') 
                        : 'No matching courses found.';
                    $('#resultsTableBody').append(
                        '<tr id="modalNoSemesterMatchRow">' +
                        '<td colspan="' + colSpan + '" class="text-center py-5 text-muted">' +
                        '<i class="bi bi-calendar-x fs-2 d-block mb-2 text-secondary"></i>' +
                        '<div class="fw-semibold text-dark mb-1">' + msg + '</div>' +
                        '<small class="text-secondary">Please select another semester or choose "All Semesters".</small>' +
                        '</td></tr>'
                    );
                }
            }

            $(document).on('change', '#modalCourseSemesterFilter', function () {
                filterResultsTableRows();
            });

            $('#resultsFilterGroup button').on('click', function () {
                $('#resultsFilterGroup button').removeClass('active');
                $(this).addClass('active');
                activeResultsFilter = $(this).data('filter');
                filterResultsTableRows();
            });

            $('#modalCourseSearch').on('input keyup', filterResultsTableRows);

            $(document).on('input change', '.course-marks-input', function () {
                const $input = $(this);
                let val = parseFloat($input.val());
                if (val < 0) { val = 0; $input.val(0); }
                if (val > 100) { val = 100; $input.val(100); }

                const $row = $input.closest('tr');
                const courseId = $row.data('course-id');

                if (!isNaN(val) && $input.val() !== '') {
                    const gp = getGradePoint(val);
                    const letter = getLetterGrade(val);
                    $row.find('.grade-point-cell').text(gp.toFixed(2));
                    const $badge = $row.find('.letter-grade-badge');
                    $badge.attr('class', getGradeBadgeClass(letter) + ' letter-grade-badge').text(letter);
                    $row.attr('data-marks', val);
                    $row.attr('data-evaluated', 'true');
                    $row.find('.status-cell').html('<span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle"><i class="bi bi-clock me-1"></i>Unpublished</span>');
                    $row.find('.action-cell').html('<button type="button" class="btn btn-xs btn-outline-success btn-single-publish" data-course-id="' + courseId + '" data-publish="true" title="Publish this course"><i class="bi bi-send-check me-1"></i>Publish</button>');
                } else {
                    $row.find('.grade-point-cell').html('<span class="text-muted">-</span>');
                    $row.find('.letter-grade-badge').attr('class', 'badge bg-secondary-subtle text-secondary letter-grade-badge').text('-');
                    $row.attr('data-marks', '');
                    $row.attr('data-evaluated', 'false');
                    $row.find('.status-cell').html('<span class="badge badge-unevaluated"><i class="bi bi-dash-circle me-1"></i>Not Evaluated</span>');
                    $row.find('.action-cell').html('<button type="button" class="btn btn-xs btn-light text-muted border btn-single-publish-disabled" disabled title="Enter marks first to publish"><i class="bi bi-lock me-1"></i>Publish</button>');
                }

                computeLiveCgpa();
            });

            function saveResults(shouldPublish) {
                if (shouldPublish === undefined) {
                    shouldPublish = permissions.canPublish;
                }
                if (!currentResultsStudentUid && !currentResultsStudentId) {
                    showToast('danger', 'Student record not found. Please reload.');
                    return;
                }

                const payloadResults = [];
                let hasInvalid = false;
                const studentJoiningSem = $('#modalStudentSem').text() || 'FALL 2025';

                $('#resultsTableBody tr').each(function () {
                    const courseId = parseInt($(this).data('course-id'));
                    if (!courseId) return;
                    const val = $(this).find('.course-marks-input').val();
                    const courseRowSem = $(this).find('.course-row-semester-select').val() || $(this).attr('data-semester') || studentJoiningSem;

                    if (val !== undefined && val !== '' && !isNaN(parseFloat(val))) {
                        const marks = parseFloat(val);
                        if (marks < 0 || marks > 100) {
                            hasInvalid = true;
                        }
                        payloadResults.push({
                            courseId: courseId,
                            marks: marks,
                            evaluated: true,
                            published: shouldPublish,
                            semester: courseRowSem
                        });
                    }
                });

                if (hasInvalid) {
                    showToast('danger', 'Please enter valid marks between 0 and 100.');
                    return;
                }

                if (payloadResults.length === 0) {
                    showToast('danger', 'No marks entered. Please enter marks (0–100) for at least one course.');
                    return;
                }

                const $btn = shouldPublish ? $('#btnPublishResults') : $('#btnSaveResults');
                const originalText = $btn.length ? $btn.html() : '';
                if ($btn.length) {
                    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span> ' + (shouldPublish ? 'Publishing...' : 'Saving...'));
                }

                $.ajax({
                    url: contextPath + '/api/students',
                    type: 'POST',
                    contentType: 'application/json; charset=UTF-8',
                    data: JSON.stringify({
                        action: 'save_results',
                        id: currentResultsStudentId,
                        uid: currentResultsStudentUid,
                        publish: shouldPublish,
                        results: payloadResults
                    }),
                    headers: { 'X-Requested-With': 'XMLHttpRequest' },
                    success: function (res) {
                        if (res.success) {
                            showToast('success', res.message || (shouldPublish ? 'Results published successfully and CGPA updated!' : 'Results saved successfully and CGPA updated!'));
                            const newCgpa = parseFloat(res.calculatedCgpa).toFixed(2);
                            $('#modalCgpaDisplay').text(newCgpa);
                            $('#footerCgpa').text(newCgpa);

                            const $studentRow = $('#student-row-' + currentResultsStudentUid);
                            if ($studentRow.length > 0) {
                                $studentRow.find('.badge-cgpa').text(newCgpa);
                            }

                            reloadResultsModal();

                            if (typeof fetchStatsOnly === 'function') {
                                fetchStatsOnly();
                            }
                        } else {
                            showToast('danger', res.message || (shouldPublish ? 'Failed to publish results.' : 'Failed to save results.'));
                        }
                    },
                    error: function (xhr) {
                        let errorMsg = shouldPublish ? 'Failed to publish results.' : 'Failed to save results.';
                        try {
                            const data = JSON.parse(xhr.responseText);
                            if (data.message) errorMsg = data.message;
                        } catch (e) { }
                        showToast('danger', errorMsg);
                    },
                    complete: function () {
                        if ($btn.length) {
                            $btn.prop('disabled', false).html(originalText);
                        }
                    }
                });
            }

            $('#btnPublishResults').on('click', function (e) {
                e.preventDefault();
                saveResults(true);
            });

            $('#btnSaveResults').on('click', function (e) {
                e.preventDefault();
                saveResults(false);
            });

            // Single Course Publish / Unpublish Toggle
            $(document).on('click', '.btn-single-publish', function (e) {
                e.preventDefault();
                if (!currentResultsStudentUid && !currentResultsStudentId) return;
                const $btn = $(this);
                const $row = $btn.closest('tr');
                const courseId = parseInt($btn.data('course-id'));
                const shouldPublish = ($btn.data('publish') === true || $btn.data('publish') === 'true');
                const courseSem = $row.find('.course-row-semester-select').val() || $row.attr('data-semester') || $('#modalStudentSem').text();

                $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm"></span>');

                $.ajax({
                    url: contextPath + '/api/students',
                    type: 'POST',
                    contentType: 'application/json; charset=UTF-8',
                    data: JSON.stringify({
                        action: 'publish_single_course',
                        id: currentResultsStudentId,
                        uid: currentResultsStudentUid,
                        courseId: courseId,
                        publish: shouldPublish,
                        semester: courseSem
                    }),
                    headers: { 'X-Requested-With': 'XMLHttpRequest' },
                    success: function (res) {
                        if (res.success) {
                            showToast('success', res.message || 'Course publication status updated.');
                            const newCgpa = parseFloat(res.calculatedCgpa).toFixed(2);
                            $('#modalCgpaDisplay').text(newCgpa);
                            $('#footerCgpa').text(newCgpa);

                            const $studentRow = $('#student-row-' + currentResultsStudentUid);
                            if ($studentRow.length > 0) {
                                $studentRow.find('.badge-cgpa').text(newCgpa);
                            }

                            reloadResultsModal();
                        } else {
                            showToast('danger', res.message || 'Failed to update course status.');
                            $btn.prop('disabled', false);
                        }
                    },
                    error: function (xhr) {
                        let err = 'Failed to update course publish state.';
                        try {
                            const d = JSON.parse(xhr.responseText);
                            if (d.message) err = d.message;
                        } catch (e) { }
                        showToast('danger', err);
                        $btn.prop('disabled', false);
                    }
                });
            });

            // Admin changes individual course row semester
            $(document).on('change', '.course-row-semester-select', function () {
                const $sel = $(this);
                const courseId = parseInt($sel.data('course-id'));
                const chosenSem = $sel.val();
                const $row = $sel.closest('tr');
                $row.attr('data-semester', chosenSem);

                if (currentResultsStudentUid && courseId) {
                    $.ajax({
                        url: contextPath + '/api/students',
                        type: 'POST',
                        contentType: 'application/json; charset=UTF-8',
                        data: JSON.stringify({
                            action: 'update_course_semester',
                            id: currentResultsStudentId,
                            uid: currentResultsStudentUid,
                            courseId: courseId,
                            semester: chosenSem
                        }),
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        success: function () {
                            // Silently persisted
                        }
                    });
                }
            });

            // ==================== Course Assignment Modal ====================
            if (permissions.canAssign) {
                let deptCoursesCache = {};

                function loadDeptCoursesForAssignment(dept) {
                    const $container = $('#assignCoursesListContainer');
                    $container.html('<div class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary"></div><span class="ms-2 small text-muted">Loading ' + escapeHtml(dept) + ' curriculum courses...</span></div>');

                    $.ajax({
                        url: contextPath + '/api/students',
                        type: 'POST',
                        contentType: 'application/json; charset=UTF-8',
                        data: JSON.stringify({ action: 'courses', department: dept }),
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        success: function (res) {
                            if (res.success && res.courses) {
                                deptCoursesCache[dept] = res.courses;
                                renderAssignCourseCheckboxes(res.courses);
                            } else {
                                $container.html('<div class="text-center py-4 text-muted small">No courses found for ' + escapeHtml(dept) + '.</div>');
                            }
                        },
                        error: function () {
                            $container.html('<div class="text-center py-4 text-danger small">Failed to load courses.</div>');
                        }
                    });
                }

                function renderAssignCourseCheckboxes(courses) {
                    const $container = $('#assignCoursesListContainer');
                    $container.empty();

                    if (!courses || courses.length === 0) {
                        $container.html('<div class="text-center py-4 text-muted small">No courses registered for this department.</div>');
                        updateAssignSelectedSummary();
                        return;
                    }

                    courses.forEach(function (c) {
                        const credits = parseFloat(c.creditHours) || 3.0;
                        const itemHtml = '<div class="course-check-item d-flex align-items-center justify-content-between">' +
                            '<div class="form-check mb-0">' +
                            '<input class="form-check-input assign-course-chk" type="checkbox" value="' + c.id + '" data-credits="' + credits + '" id="assignChk' + c.id + '">' +
                            '<label class="form-check-label fw-medium text-dark ms-2" for="assignChk' + c.id + '">' +
                            '<span class="font-monospace fw-semibold text-primary me-2">' + escapeHtml(c.courseCode) + '</span>' +
                            '<span>' + escapeHtml(c.courseName) + '</span>' +
                            '</label>' +
                            '</div>' +
                            '<span class="badge bg-light text-secondary border font-monospace">' + credits.toFixed(1) + ' Credits</span>' +
                            '</div>';
                        $container.append(itemHtml);
                    });

                    updateAssignSelectedSummary();
                }

                function updateAssignSelectedSummary() {
                    let count = 0;
                    let credits = 0;
                    $('.assign-course-chk:checked').each(function () {
                        count++;
                        credits += parseFloat($(this).data('credits')) || 3.0;
                    });
                    $('#assignSelectedSummary').text(count + ' courses selected (' + credits.toFixed(1) + ' credits)');
                }

                function updateDeptStudentsDropdown(dept) {
                    const $select = $('#assignStudentSelect');
                    $select.empty();
                    let count = 0;

                    $('.student-item').each(function () {
                        const sDept = ($(this).data('dept') || '').toString().toUpperCase();
                        if (sDept === dept.toUpperCase()) {
                            const uid = $(this).data('uid');
                            const name = $(this).data('name');
                            $select.append('<option value="' + uid + '">' + escapeHtml(name) + ' (' + uid + ')</option>');
                            count++;
                        }
                    });

                    if (count === 0) {
                        $select.html('<option value="">No students in ' + escapeHtml(dept) + ' yet</option>');
                    }
                }

                $('#btnOpenAssignCoursesModal').on('click', function (e) {
                    e.preventDefault();
                    const initialDept = $('#assignDeptSelect').val() || 'CSE';
                    loadDeptCoursesForAssignment(initialDept);
                    updateDeptStudentsDropdown(initialDept);
                    if (assignCoursesModal) assignCoursesModal.show();
                });

                $('#assignDeptSelect').on('change', function () {
                    const dept = $(this).val();
                    loadDeptCoursesForAssignment(dept);
                    updateDeptStudentsDropdown(dept);
                });

                $('input[name="assignTargetRadio"]').on('change', function () {
                    if ($(this).val() === 'SPECIFIC') {
                        $('#assignSpecificStudentWrapper').removeClass('d-none');
                    } else {
                        $('#assignSpecificStudentWrapper').addClass('d-none');
                    }
                });

                $(document).on('change', '.assign-course-chk', updateAssignSelectedSummary);

                $('#btnAssignSelectAll').on('click', function () {
                    $('.assign-course-chk').prop('checked', true);
                    updateAssignSelectedSummary();
                });

                $('#btnAssignDeselectAll').on('click', function () {
                    $('.assign-course-chk').prop('checked', false);
                    updateAssignSelectedSummary();
                });

                $('#btnSubmitAssignCourses').on('click', function () {
                    const selectedCourseIds = [];
                    $('.assign-course-chk:checked').each(function () {
                        selectedCourseIds.push(parseInt($(this).val()));
                    });

                    if (selectedCourseIds.length === 0) {
                        showToast('danger', 'Please select at least one course to assign.');
                        return;
                    }

                    const dept = $('#assignDeptSelect').val();
                    const sem = $('#assignSemesterSelect').val();
                    const isSpecific = ($('input[name="assignTargetRadio"]:checked').val() === 'SPECIFIC');
                    const target = isSpecific ? $('#assignStudentSelect').val() : 'ALL';

                    if (isSpecific && (!target || target.trim() === '')) {
                        showToast('danger', 'Please select a specific student.');
                        return;
                    }

                    const $btn = $(this);
                    const orig = $btn.html();
                    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span> Assigning...');

                    $.ajax({
                        url: contextPath + '/api/students',
                        type: 'POST',
                        contentType: 'application/json; charset=UTF-8',
                        data: JSON.stringify({
                            action: 'assign_courses',
                            department: dept,
                            semester: sem,
                            target: target,
                            courseIds: selectedCourseIds
                        }),
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        success: function (res) {
                            if (res.success) {
                                showToast('success', res.message || 'Courses assigned successfully!');
                                if (assignCoursesModal) assignCoursesModal.hide();
                                loadStudents();
                            } else {
                                showToast('danger', res.message || 'Failed to assign courses.');
                            }
                        },
                        error: function (xhr) {
                            let msg = 'Failed to assign courses.';
                            try {
                                const d = JSON.parse(xhr.responseText);
                                if (d.message) msg = d.message;
                            } catch (e) { }
                            showToast('danger', msg);
                        },
                        complete: function () {
                            $btn.prop('disabled', false).html(orig);
                        }
                    });
                });
            }

            // ==================== Student Course Registration Modal ====================
            if (!isStaff) {
                let upcomingSemesterName = 'SPRING 2026';

                function loadStudentUpcomingRegistration() {
                    $('#studentRegDeptDisplay').text(currentStudentDept || 'General');


                    $.ajax({
                        url: contextPath + '/api/students',
                        type: 'POST',
                        contentType: 'application/json; charset=UTF-8',
                        data: JSON.stringify({ action: 'semester_info' }),
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        success: function (res) {
                            if (res.success && res.upcomingSemester) {
                                upcomingSemesterName = res.upcomingSemester;
                                $('#studentRegisterUpcomingSemName').text(res.upcomingSemester);
                                if (res.upcomingSemesterDuration) {
                                    $('#studentRegisterUpcomingSemDuration').text('(' + res.upcomingSemesterDuration + ')');
                                }
                            }
                        }
                    });

                    const $container = $('#studentRegisterCoursesContainer');
                    $container.html('<div class="text-center py-4"><div class="spinner-border spinner-border-sm text-success"></div><span class="ms-2 small text-muted">Loading courses for your department...</span></div>');

                    $.ajax({
                        url: contextPath + '/api/students',
                        type: 'POST',
                        contentType: 'application/json; charset=UTF-8',
                        data: JSON.stringify({ action: 'courses', department: currentStudentDept }),
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        success: function (res) {
                            if (res.success && res.courses) {
                                renderStudentCourseCheckboxes(res.courses);
                            } else {
                                $container.html('<div class="text-center py-4 text-muted small">No courses found for your department.</div>');
                            }
                        },
                        error: function () {
                            $container.html('<div class="text-center py-4 text-danger small">Failed to load department courses.</div>');
                        }
                    });
                }

                function renderStudentCourseCheckboxes(courses) {
                    const $container = $('#studentRegisterCoursesContainer');
                    $container.empty();

                    if (!courses || courses.length === 0) {
                        $container.html('<div class="text-center py-4 text-muted small">No courses available for registration.</div>');
                        updateStudentRegSummary();
                        return;
                    }

                    courses.forEach(function (c) {
                        const credits = parseFloat(c.creditHours) || 3.0;
                        const itemHtml = '<div class="course-check-item d-flex align-items-center justify-content-between">' +
                            '<div class="form-check mb-0">' +
                            '<input class="form-check-input student-reg-course-chk" type="checkbox" value="' + c.id + '" data-credits="' + credits + '" id="studentRegChk' + c.id + '">' +
                            '<label class="form-check-label fw-medium text-dark ms-2" for="studentRegChk' + c.id + '">' +
                            '<span class="font-monospace fw-semibold text-primary me-2">' + escapeHtml(c.courseCode) + '</span>' +
                            '<span>' + escapeHtml(c.courseName) + '</span>' +
                            '</label>' +
                            '</div>' +
                            '<span class="badge bg-light text-secondary border font-monospace">' + credits.toFixed(1) + ' Credits</span>' +
                            '</div>';
                        $container.append(itemHtml);
                    });

                    updateStudentRegSummary();
                }

                function updateStudentRegSummary() {
                    let count = 0;
                    let credits = 0;
                    $('.student-reg-course-chk:checked').each(function () {
                        count++;
                        credits += parseFloat($(this).data('credits')) || 3.0;
                    });
                    $('#studentRegTotalCount').text(count);
                    $('#studentRegTotalCredits').text(credits.toFixed(1));
                }

                $('#btnOpenStudentRegisterModal').on('click', function (e) {
                    e.preventDefault();
                    loadStudentUpcomingRegistration();
                    if (studentRegisterModal) studentRegisterModal.show();
                });

                $(document).on('change', '.student-reg-course-chk', updateStudentRegSummary);

                $('#btnStudentRegSelectAll').on('click', function () {
                    $('.student-reg-course-chk').prop('checked', true);
                    updateStudentRegSummary();
                });

                $('#btnStudentRegDeselectAll').on('click', function () {
                    $('.student-reg-course-chk').prop('checked', false);
                    updateStudentRegSummary();
                });

                $('#btnSubmitStudentRegister').on('click', function () {
                    const selectedCourseIds = [];
                    $('.student-reg-course-chk:checked').each(function () {
                        selectedCourseIds.push(parseInt($(this).val()));
                    });

                    if (selectedCourseIds.length === 0) {
                        showToast('danger', 'Please select at least one course to register for upcoming semester.');
                        return;
                    }

                    if (!currentStudentUid) {
                        showToast('danger', 'Student identification not detected. Please reload.');
                        return;
                    }

                    const $btn = $(this);
                    const orig = $btn.html();
                    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span> Registering...');

                    $.ajax({
                        url: contextPath + '/api/students',
                        type: 'POST',
                        contentType: 'application/json; charset=UTF-8',
                        data: JSON.stringify({
                            action: 'register_courses',
                            uid: currentStudentUid,
                            semester: upcomingSemesterName,
                            courseIds: selectedCourseIds
                        }),
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        success: function (res) {
                            if (res.success) {
                                showToast('success', res.message || 'Courses registered successfully for upcoming semester!');
                                if (studentRegisterModal) studentRegisterModal.hide();
                                loadStudents();
                            } else {
                                showToast('danger', res.message || 'Failed to register courses.');
                            }
                        },
                        error: function (xhr) {
                            let msg = 'Failed to register courses.';
                            try {
                                const d = JSON.parse(xhr.responseText);
                                if (d.message) msg = d.message;
                            } catch (e) { }
                            showToast('danger', msg);
                        },
                        complete: function () {
                            $btn.prop('disabled', false).html(orig);
                        }
                    });
                });
            }

            // ==================== Delete Student Handlers ====================
            if (permissions.canDelete) {
                $(document).on('click', '.btn-delete', function (e) {
                    e.preventDefault();
                    studentToDeleteId = $(this).data('id');
                    studentToDeleteUid = $(this).data('uid');
                    const studentName = $(this).data('name');
                    $('#deleteStudentNameDisplay').html((studentToDeleteId ? '<span class="badge bg-primary text-white me-2">ID #' + studentToDeleteId + '</span>' : '') + escapeHtml(studentName));
                    if (deleteModal) deleteModal.show();
                });

                $('#confirmDeleteBtn').on('click', function () {
                    if (!studentToDeleteId && !studentToDeleteUid) return;
                    const $btn = $(this);
                    $btn.prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-1"></span> Deleting...');

                    $.ajax({
                        url: contextPath + '/api/students',
                        type: 'POST',
                        contentType: 'application/json; charset=UTF-8',
                        data: JSON.stringify({ action: 'delete', id: studentToDeleteId, uid: studentToDeleteUid }),
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        success: function (response) {
                            if (deleteModal) deleteModal.hide();
                            if (response.success) {
                                const rowSel = studentToDeleteId ? ('#student-row-' + studentToDeleteId + ', #student-row-' + studentToDeleteUid) : ('#student-row-' + studentToDeleteUid);
                                $(rowSel).addClass('table-danger').fadeOut(400, function () {
                                    $(this).remove();
                                    showToast('success', response.message || 'Student deleted successfully.');
                                    if (typeof fetchStatsOnly === 'function') fetchStatsOnly();
                                    if ($('.student-item').length === 0) renderTable([]);
                                });
                            } else {
                                showToast('danger', response.message || 'Failed to delete student.');
                            }
                        },
                        error: function (xhr) {
                            if (deleteModal) deleteModal.hide();
                            let errorMsg = 'Failed to delete student.';
                            try {
                                const data = JSON.parse(xhr.responseText);
                                if (data.message) errorMsg = data.message;
                            } catch (e) { }
                            showToast('danger', errorMsg);
                        },
                        complete: function () {
                            $btn.prop('disabled', false).html('<i class="bi bi-trash3 me-1"></i> Delete Student');
                            studentToDeleteId = null;
                            studentToDeleteUid = null;
                        }
                    });
                });
            }

            if (isStaff) {
                function fetchStatsOnly() {
                    $.ajax({
                        url: contextPath + '/api/students',
                        type: 'POST',
                        contentType: 'application/json; charset=UTF-8',
                        data: JSON.stringify({ action: 'stats' }),
                        headers: { 'X-Requested-With': 'XMLHttpRequest' },
                        success: function (res) {
                            if (res.success) {
                                $('#statTotal').text(res.total);
                                $('#statAvgCgpa').text(parseFloat(res.avgCgpa).toFixed(2));
                                $('#statHighestCgpa').text(parseFloat(res.highestCgpa).toFixed(2));
                                $('#statDeptCount').text(res.departmentCount);
                            }
                        }
                    });
                }

                fetchStatsOnly();
            }

            $('#btnRefresh').on('click', function (e) {
                e.preventDefault();
                loadStudents();
            });
        });
    </script>
</body>

</html>
