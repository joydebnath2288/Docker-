package com.studentmanagement.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final Properties properties = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (Exception e) {
            System.err.println("Could not load db.properties: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found in classpath!");
            throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
        }

        String url = getVal("DB_URL", "db.url", "jdbc:mysql://localhost:3306/student_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        String user = getVal("DB_USER", "db.user", "root");
        String password = getVal("DB_PASSWORD", "db.password", "Joydeb123#");

        Connection conn = DriverManager.getConnection(url, user, password);
        checkMigrations(conn);
        return conn;
    }

    private static volatile boolean migrated = false;

    private static void checkMigrations(Connection conn) {
        if (!migrated) {
            synchronized (DBConnection.class) {
                if (!migrated) {
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        // 1. Remove legacy username/password columns if present
                        boolean hasUsername = false;
                        try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM students LIKE 'username'")) {
                            hasUsername = rs.next();
                        }
                        if (hasUsername) {
                            try { stmt.executeUpdate("ALTER TABLE students DROP FOREIGN KEY fk_student_user"); } catch (SQLException ignored) {}
                            try { stmt.executeUpdate("ALTER TABLE students DROP COLUMN password"); } catch (SQLException ignored) {}
                            try { stmt.executeUpdate("ALTER TABLE students DROP COLUMN username"); } catch (SQLException ignored) {}
                        }

                        // 2. Unify ID: Ensure students.id matches users.id and drop redundant user_id column
                        boolean hasUserId = false;
                        try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM students LIKE 'user_id'")) {
                            hasUserId = rs.next();
                        }

                        if (hasUserId) {
                            try { stmt.executeUpdate("ALTER TABLE students DROP FOREIGN KEY fk_student_user_id"); } catch (SQLException ignored) {}
                            try { stmt.executeUpdate("ALTER TABLE students DROP INDEX uq_student_user_id"); } catch (SQLException ignored) {}
                            try { stmt.executeUpdate("ALTER TABLE students MODIFY id INT NOT NULL"); } catch (SQLException ignored) {}
                            try { stmt.executeUpdate("UPDATE students SET id = user_id WHERE user_id IS NOT NULL"); } catch (SQLException ignored) {}
                            try { stmt.executeUpdate("ALTER TABLE students DROP COLUMN user_id"); } catch (SQLException ignored) {}
                        }

                        // 3. Ensure foreign key linking students.id directly to users.id
                        try {
                            stmt.executeUpdate("ALTER TABLE students ADD CONSTRAINT fk_students_users FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE");
                        } catch (SQLException ignored) {}

                        try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM students LIKE 'semester'")) {
                            if (!rs.next()) {
                                stmt.executeUpdate("ALTER TABLE students ADD COLUMN semester VARCHAR(50) DEFAULT 'FALL 2025'");
                            }
                        }
                        stmt.executeUpdate("UPDATE students SET semester = 'FALL 2025' WHERE semester IS NULL OR TRIM(semester) = ''");

                        boolean adminPasswordValid = false;
                        try (java.sql.ResultSet rs = stmt.executeQuery("SELECT password FROM users WHERE username = 'admin'")) {
                            if (rs.next()) {
                                String currentHash = rs.getString("password");
                                adminPasswordValid = com.studentmanagement.util.PasswordUtil.checkPassword("admin", currentHash);
                            } else {
                                String adminHash = com.studentmanagement.util.PasswordUtil.hashPassword("admin");
                                stmt.executeUpdate("INSERT INTO users (username, password, role, status) VALUES ('admin', '" + adminHash + "', 1, 1)");
                                adminPasswordValid = true;
                            }
                        }
                        if (!adminPasswordValid) {
                            String adminHash = com.studentmanagement.util.PasswordUtil.hashPassword("admin");
                            stmt.executeUpdate("UPDATE users SET password = '" + adminHash + "', role = 1, status = 1 WHERE username = 'admin'");
                        }

                        // Seed roles table if it doesn't exist
                        try {
                            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS roles (" +
                                    "id INT PRIMARY KEY, " +
                                    "name VARCHAR(50) NOT NULL UNIQUE, " +
                                    "display_name VARCHAR(100) NOT NULL, " +
                                    "description VARCHAR(255))");
                            stmt.executeUpdate("INSERT IGNORE INTO roles (id, name, display_name, description) VALUES " +
                                    "(1, 'ADMIN', 'Administrator', 'Full system access, student deletion, and configuration'), " +
                                    "(2, 'STUDENT', 'Student', 'Personal academic record and published results'), " +
                                    "(3, 'FACULTY', 'Faculty', 'View students, assign courses, evaluate and enter course marks'), " +
                                    "(4, 'REGISTRAR', 'Registrar', 'Student admissions, add and edit student records, course enrollment'), " +
                                    "(5, 'ACCOUNTS', 'Accounts', 'View student directory and enrollment records for tuition and fees'), " +
                                    "(6, 'SUPERVISOR', 'Supervisor', 'Academic oversight, review results, and approve result publication')");
                        } catch (SQLException ignored) {}

                        // Seed demo accounts for each role
                        seedUserIfNotExists(stmt, "supervisor", "supervisor123", 6);
                        seedUserIfNotExists(stmt, "faculty", "faculty123", 3);
                        seedUserIfNotExists(stmt, "registrar", "registrar123", 4);
                        seedUserIfNotExists(stmt, "accounts", "accounts123", 5);

                        // Clean up orphaned student user accounts (role = 2 with no corresponding student record)
                        try {
                            stmt.executeUpdate("DELETE FROM users WHERE role = 2 AND id NOT IN (SELECT id FROM students)");
                        } catch (SQLException ignored) {}

                        // 4. Migrate student_results: add student_id (FK to students.id) and drop student_uid
                        try {
                            boolean hasStudentUid = false;
                            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM student_results LIKE 'student_uid'")) {
                                hasStudentUid = rs.next();
                            }

                            boolean hasStudentId = false;
                            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM student_results LIKE 'student_id'")) {
                                hasStudentId = rs.next();
                            }

                            if (hasStudentUid) {
                                if (!hasStudentId) {
                                    try {
                                        stmt.executeUpdate("ALTER TABLE student_results ADD COLUMN student_id INT AFTER id");
                                    } catch (SQLException ignored) {}
                                }

                                try {
                                    stmt.executeUpdate("UPDATE student_results r JOIN students s ON r.student_uid = s.uid SET r.student_id = s.id");
                                } catch (SQLException ignored) {}

                                try {
                                    stmt.executeUpdate("DELETE FROM student_results WHERE student_id IS NULL OR student_id <= 0");
                                } catch (SQLException ignored) {}

                                // Drop foreign key on student_uid
                                try {
                                    java.util.List<String> fksToDrop = new java.util.ArrayList<>();
                                    try (java.sql.ResultSet rsFk = stmt.executeQuery(
                                            "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'student_results' AND (COLUMN_NAME = 'student_uid' OR REFERENCED_COLUMN_NAME = 'uid')")) {
                                        while (rsFk.next()) {
                                            fksToDrop.add(rsFk.getString(1));
                                        }
                                    }
                                    for (String fk : fksToDrop) {
                                        try { stmt.executeUpdate("ALTER TABLE student_results DROP FOREIGN KEY " + fk); } catch (SQLException ignored) {}
                                    }
                                } catch (SQLException ignored) {}

                                // Drop old unique constraint on (student_uid, course_id)
                                try { stmt.executeUpdate("ALTER TABLE student_results DROP INDEX uq_student_course"); } catch (SQLException ignored) {}

                                // Drop student_uid column
                                try { stmt.executeUpdate("ALTER TABLE student_results DROP COLUMN student_uid"); } catch (SQLException ignored) {}

                                // Modify student_id to NOT NULL
                                try { stmt.executeUpdate("ALTER TABLE student_results MODIFY student_id INT NOT NULL"); } catch (SQLException ignored) {}
                            }

                            // Ensure foreign key fk_result_student exists
                            try {
                                stmt.executeUpdate("ALTER TABLE student_results ADD CONSTRAINT fk_result_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE");
                            } catch (SQLException ignored) {}

                            // Ensure unique constraint uq_student_course exists
                            try {
                                stmt.executeUpdate("ALTER TABLE student_results ADD CONSTRAINT uq_student_course UNIQUE (student_id, course_id)");
                            } catch (SQLException ignored) {}
                        } catch (SQLException ignored) {}

                        com.studentmanagement.dao.CourseDAO.initTablesAndSeed(conn);

                        migrated = true;
                    } catch (SQLException e) {
                        // Silent check
                    }
                }
            }
        }
    }

    private static void seedUserIfNotExists(java.sql.Statement stmt, String username, String password, int roleId) {
        try (java.sql.ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE username = '" + username + "'")) {
            if (!rs.next()) {
                String hash = com.studentmanagement.util.PasswordUtil.hashPassword(password);
                stmt.executeUpdate("INSERT INTO users (username, password, role, status) VALUES ('" + username + "', '" + hash + "', " + roleId + ", 1)");
            }
        } catch (SQLException ignored) {}
    }

    private static String getVal(String envKey, String propKey, String defaultValue) {
        String val = System.getenv(envKey);
        if (val == null || val.trim().isEmpty()) {
            val = System.getProperty(propKey);
        }
        if (val == null || val.trim().isEmpty()) {
            val = properties.getProperty(propKey, defaultValue);
        }
        return val != null ? val : defaultValue;
    }
}
