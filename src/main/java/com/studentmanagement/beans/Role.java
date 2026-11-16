package com.studentmanagement.beans;

public enum Role {

    ADMIN(1, "ADMIN", "Administrator", "Full system access, student deletion, and configuration"),
    STUDENT(2, "STUDENT", "Student", "Personal academic record and published results"),
    FACULTY(3, "FACULTY", "Faculty", "View students, assign courses, evaluate and enter course marks"),
    REGISTRAR(4, "REGISTRAR", "Registrar", "Student admissions, add and edit student records, course enrollment"),
    ACCOUNTS(5, "ACCOUNTS", "Accounts", "View student directory and enrollment records for tuition and fees"),
    SUPERVISOR(6, "SUPERVISOR", "Supervisor", "Academic oversight, review results, and approve result publication");

    private final int id;
    private final String code;
    private final String displayName;
    private final String description;

    Role(int id, String code, String displayName, String description) {
        this.id = id;
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

   
    public static Role fromId(int id) {
        for (Role r : values()) {
            if (r.id == id) {
                return r;
            }
        }
        return null;
    }

    /**
     * Resolves a Role by its name or code (case-insensitive).
     */
    public static Role fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        String clean = code.trim();
        for (Role r : values()) {
            if (r.code.equalsIgnoreCase(clean) || r.name().equalsIgnoreCase(clean) || r.displayName.equalsIgnoreCase(clean)) {
                return r;
            }
        }
        if ("REGISTER".equalsIgnoreCase(clean) || "REGISTRATION".equalsIgnoreCase(clean)) {
            return REGISTRAR;
        }
        if ("ACCOUNT".equalsIgnoreCase(clean) || "ACOUNTS".equalsIgnoreCase(clean)) {
            return ACCOUNTS;
        }
        return null;
    }

    public static boolean isValidRoleId(int id) {
        return fromId(id) != null;
    }

  

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isStudent() {
        return this == STUDENT;
    }

    public boolean isFaculty() {
        return this == FACULTY;
    }

    public boolean isRegistrar() {
        return this == REGISTRAR;
    }

    public boolean isAccounts() {
        return this == ACCOUNTS;
    }

    public boolean isSupervisor() {
        return this == SUPERVISOR;
    }

 
    public boolean isStaff() {
        return this != STUDENT;
    }


    public boolean canViewDirectory() {
        return this != STUDENT;
    }


    public boolean canAddStudent() {
        return this == ADMIN || this == REGISTRAR;
    }

 
    public boolean canEditStudent() {
        return this == ADMIN || this == REGISTRAR;
    }

  
    public boolean canDeleteStudent() {
        return this == ADMIN;
    }


    public boolean canAssignCourses() {
        return this == ADMIN || this == REGISTRAR || this == FACULTY;
    }


    public boolean canGradeResults() {
        return this == ADMIN || this == FACULTY;
    }


    public boolean canPublishResults() {
        return this == ADMIN || this == SUPERVISOR;
    }


    public boolean canViewResults() {
        return true;
    }
}
