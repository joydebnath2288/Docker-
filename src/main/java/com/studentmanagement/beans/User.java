package com.studentmanagement.beans;

public class User {

    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_STUDENT = 2;
    public static final int ROLE_FACULTY = 3;
    public static final int ROLE_REGISTRAR = 4;
    public static final int ROLE_ACCOUNTS = 5;
    public static final int ROLE_SUPERVISOR = 6;

    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;

    private int id;
    private String username;
    private String password;
    private int roleId = ROLE_STUDENT;
    private int status = STATUS_ACTIVE;
    private Student student;

    public User() {
    }

    public User(int id, String username, String password) {
        this(id, username, password, ROLE_STUDENT, STATUS_ACTIVE);
    }

    public User(int id, String username, String password, int roleId, int status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roleId = roleId;
        this.status = status;
    }

    public User(int id, String username, String password, int roleId, int status, Student student) {
        this(id, username, password, roleId, status);
        setStudent(student);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        if (student != null && id > 0) {
            Integer curId = student.getUserId();
            if (curId == null || curId.intValue() != id) {
                student.setUserId(id);
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public Role getRole() {
        Role r = Role.fromId(this.roleId);
        return r != null ? r : Role.STUDENT;
    }

    public String getRoleName() {
        return getRole().getDisplayName();
    }

    public String getRoleCode() {
        return getRole().getCode();
    }

    public void setRole(String role) {
        Role r = Role.fromCode(role);
        this.roleId = (r != null) ? r.getId() : ROLE_STUDENT;
    }

    /**
     * Bootstrap badge CSS class matching the user's role.
     */
    public String getBadgeClass() {
        switch (getRole()) {
            case ADMIN:
                return "bg-danger";
            case SUPERVISOR:
                return "bg-dark";
            case FACULTY:
                return "bg-info text-dark";
            case REGISTRAR:
                return "bg-success";
            case ACCOUNTS:
                return "bg-warning text-dark";
            case STUDENT:
            default:
                return "bg-primary";
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isAdmin() {
        return getRole().isAdmin();
    }

    public boolean isStudent() {
        return getRole().isStudent();
    }

    public boolean isFaculty() {
        return getRole().isFaculty();
    }

    public boolean isRegistrar() {
        return getRole().isRegistrar();
    }

    public boolean isAccounts() {
        return getRole().isAccounts();
    }

    public boolean isSupervisor() {
        return getRole().isSupervisor();
    }

    public boolean isStaff() {
        return getRole().isStaff();
    }

    public boolean isActive() {
        return this.status == STATUS_ACTIVE;
    }

    public boolean isCanAddStudent() {
        return getRole().canAddStudent();
    }

    public boolean isCanEditStudent() {
        return getRole().canEditStudent();
    }

    public boolean isCanDeleteStudent() {
        return getRole().canDeleteStudent();
    }

    public boolean isCanAssignCourses() {
        return getRole().canAssignCourses();
    }

    public boolean isCanGradeResults() {
        return getRole().canGradeResults();
    }

    public boolean isCanPublishResults() {
        return getRole().canPublishResults();
    }

    public boolean isCanViewDirectory() {
        return getRole().canViewDirectory();
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
        if (student != null && student.getUser() != this) {
            student.setUser(this);
        }
    }
}
