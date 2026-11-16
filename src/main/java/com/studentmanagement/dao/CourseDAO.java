package com.studentmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.studentmanagement.beans.Course;
import com.studentmanagement.config.DBConnection;

public class CourseDAO {

    private static final String SELECT_COURSES_BY_DEPT =
            "SELECT id, course_code, course_name, department, credit_hours FROM courses WHERE UPPER(department) = UPPER(?) ORDER BY course_code ASC, id ASC";

    private static final String SELECT_ALL_COURSES =
            "SELECT id, course_code, course_name, department, credit_hours FROM courses ORDER BY department ASC, course_code ASC";

    private static final String SELECT_COURSE_BY_ID =
            "SELECT id, course_code, course_name, department, credit_hours FROM courses WHERE id = ?";

    public List<Course> getCoursesByDepartment(String department) {
        List<Course> list = new ArrayList<>();
        if (department == null || department.trim().isEmpty()) {
            return list;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_COURSES_BY_DEPT)) {
            ps.setString(1, department.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCourse(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching courses for department " + department + ": " + e.getMessage());
        }
        return list;
    }

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_COURSES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCourse(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all courses: " + e.getMessage());
        }
        return list;
    }

    public Course getCourseById(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_COURSE_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCourse(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course by id " + id + ": " + e.getMessage());
        }
        return null;
    }

    private Course mapCourse(ResultSet rs) throws SQLException {
        return new Course(
                rs.getInt("id"),
                rs.getString("course_code"),
                rs.getString("course_name"),
                rs.getString("department"),
                rs.getDouble("credit_hours")
        );
    }

    public static void initTablesAndSeed(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS courses (" +
                    "    id INT PRIMARY KEY AUTO_INCREMENT," +
                    "    course_code VARCHAR(30) NOT NULL," +
                    "    course_name VARCHAR(150) NOT NULL," +
                    "    department VARCHAR(50) NOT NULL," +
                    "    credit_hours DECIMAL(3, 1) NOT NULL DEFAULT 3.0," +
                    "    CONSTRAINT uq_course_dept UNIQUE (course_code, department)" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS student_results (" +
                    "    id INT PRIMARY KEY AUTO_INCREMENT," +
                    "    student_id INT NOT NULL," +
                    "    course_id INT NOT NULL," +
                    "    marks DECIMAL(5, 2) DEFAULT 0.00," +
                    "    grade_point DECIMAL(3, 2) NOT NULL DEFAULT 0.00," +
                    "    letter_grade VARCHAR(5) DEFAULT 'F'," +
                    "    published BOOLEAN NOT NULL DEFAULT FALSE," +
                    "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "    CONSTRAINT fk_result_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE," +
                    "    CONSTRAINT fk_result_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE," +
                    "    CONSTRAINT uq_student_course UNIQUE (student_id, course_id)" +
                    ")"
            );

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM courses")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; 
                }
            }

            String insertSql = "INSERT IGNORE INTO courses (course_code, course_name, department, credit_hours) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                // CSE
                addCourse(ps, "CSE1101", "Introduction to Computer Studies", "CSE", 3.0);
                addCourse(ps, "CSE1102", "Introduction to Programming", "CSE", 3.0);
                addCourse(ps, "CSE1103", "Introduction to Programming Lab", "CSE", 1.0);
                addCourse(ps, "MAT1101", "Differential Calculus & Co-ordinate Geometry", "CSE", 3.0);
                addCourse(ps, "PHY1101", "Physics 1", "CSE", 3.0);
                addCourse(ps, "PHY1102", "Physics 1 Lab", "CSE", 1.0);
                addCourse(ps, "MAT1205", "Discrete Mathematics", "CSE", 3.0);
                addCourse(ps, "MAT1202", "Integral Calculus & Ordinary Differential Equations", "CSE", 3.0);
                addCourse(ps, "CSE1204", "Object Oriented Programming 1", "CSE", 3.0);
                addCourse(ps, "PHY1203", "Physics 2", "CSE", 3.0);
                addCourse(ps, "PHY1204", "Physics 2 Lab", "CSE", 1.0);
                addCourse(ps, "ENG1201", "English Writing Skills & Communications", "CSE", 3.0);
                addCourse(ps, "EEE1201", "Introduction to Electrical Circuits and Lab", "CSE", 3.0);
                addCourse(ps, "CHE1101", "Chemistry", "CSE", 3.0);
                addCourse(ps, "MAT2101", "Complex Variable, Laplace & Z-Transformation", "CSE", 3.0);
                addCourse(ps, "CSE2107", "Introduction to Database", "CSE", 3.0);
                addCourse(ps, "EEE2103", "Electronic Devices and Lab", "CSE", 3.0);
                addCourse(ps, "BBA1102", "Principles of Accounting", "CSE", 3.0);
                addCourse(ps, "CSE2108", "Data Structure and Data Structure Lab", "CSE", 3.0);
                addCourse(ps, "CSE2201", "Algorithms", "CSE", 3.0);
                addCourse(ps, "MAT2202", "Matrices, Vectors, Fourier Analysis", "CSE", 3.0);
                addCourse(ps, "CSE2202", "Object Oriented Programming 2", "CSE", 3.0);
                addCourse(ps, "CSE3101", "Object Oriented Analysis and Design", "CSE", 3.0);
                addCourse(ps, "BAS2101", "Bangladesh Studies", "CSE", 3.0);
                addCourse(ps, "COE2101", "Digital Logic and Circuits and Lab", "CSE", 3.0);
                addCourse(ps, "MAT3103", "Computational Statistics and Probability", "CSE", 3.0);
                addCourse(ps, "CSE3102", "Theory of Computation", "CSE", 3.0);
                addCourse(ps, "ECO3101", "Principles of Economics", "CSE", 3.0);
                addCourse(ps, "ENG2101", "Business Communication", "CSE", 3.0);
                addCourse(ps, "MAT3201", "Numerical Methods for Science and Engineering", "CSE", 3.0);
                addCourse(ps, "CSE3201", "Data Communication", "CSE", 3.0);
                addCourse(ps, "COE3202", "Microprocessor and Embedded Systems", "CSE", 3.0);
                addCourse(ps, "CSE3203", "Software Engineering", "CSE", 3.0);
                addCourse(ps, "CSE4101", "Artificial Intelligence and Expert System", "CSE", 3.0);
                addCourse(ps, "CSE4102", "Computer Networks", "CSE", 3.0);
                addCourse(ps, "COE4101", "Computer Organization and Architecture", "CSE", 3.0);
                addCourse(ps, "CSE4103", "Operating System", "CSE", 3.0);
                addCourse(ps, "CSE4201", "Web Technologies", "CSE", 3.0);
                addCourse(ps, "CSE4202", "Engineering Ethics", "CSE", 3.0);
                addCourse(ps, "CSE4203", "Compiler Design", "CSE", 3.0);
                addCourse(ps, "CSE4204", "Computer Graphics", "CSE", 3.0);
                addCourse(ps, "MGT4201", "Engineering Management", "CSE", 3.0);
                addCourse(ps, "CSE4205", "Research Methodology", "CSE", 3.0);
                addCourse(ps, "CSE4299", "Thesis/Project", "CSE", 3.0);
                addCourse(ps, "CSE4300", "Internship", "CSE", 3.0);

                // DATA SCIENCE
                addCourse(ps, "DS1101", "Introduction to Computer Studies", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS1102", "Introduction to Programming", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS1103", "Introduction to Programming Lab", "DATA SCIENCE", 1.0);
                addCourse(ps, "MAT1102", "Differential Calculus and Coordinate Geometry", "DATA SCIENCE", 3.0);
                addCourse(ps, "PHY1103", "Physics 1 and Physics 1 Lab", "DATA SCIENCE", 3.0);
                addCourse(ps, "ENG1101", "English Reading Skills & Public Speaking", "DATA SCIENCE", 3.0);
                addCourse(ps, "BAS1101", "Bangladesh Studies", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS1201", "Object Oriented Programming 1 (Java)", "DATA SCIENCE", 3.0);
                addCourse(ps, "MAT1201", "Discrete Mathematics", "DATA SCIENCE", 3.0);
                addCourse(ps, "ACT1201", "Principles of Accounting", "DATA SCIENCE", 3.0);
                addCourse(ps, "MAT1203", "Integral Calculus & Ordinary Differential Equation", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS1202", "Data Structure and Data Structure Lab", "DATA SCIENCE", 3.0);
                addCourse(ps, "ENG1202", "English Writing Skills & Communications", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS2101", "Introduction to Data Science", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS2102", "Introduction to Database", "DATA SCIENCE", 3.0);
                addCourse(ps, "ECO2101", "Principles of Economics", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS2201", "Algorithms", "DATA SCIENCE", 3.0);
                addCourse(ps, "MAT2201", "Complex Variable, Laplace & Z-Transformation", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS2202", "Object Oriented Programming 2", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS2203", "Object Oriented Analysis and Design", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS3101", "Programming for Data Science", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS3102", "Ethics in Data Science & Engineering", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS3103", "Data Warehousing and Data Mining", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS3201", "Introduction to Network Engineering and Lab", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS3202", "Research Methodology", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS3203", "Programming in Python", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS4101", "Big Data Architecture", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS4102", "Machine Learning & Expert Systems", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS4299", "Project & Thesis", "DATA SCIENCE", 3.0);
                addCourse(ps, "DS4300", "Internship", "DATA SCIENCE", 3.0);

                // LAW
                addCourse(ps, "LAW1101", "Jurisprudence", "LAW", 3.0);
                addCourse(ps, "LAW1102", "Legal System of Bangladesh", "LAW", 3.0);
                addCourse(ps, "ENG1103", "Intermediate English", "LAW", 3.0);
                addCourse(ps, "LAW1103", "Legal History", "LAW", 3.0);
                addCourse(ps, "LAW1104", "Law of Contract", "LAW", 3.0);
                addCourse(ps, "LAW1100", "Freshman Orientation", "LAW", 1.0);
                addCourse(ps, "ENG1203", "Public Speaking", "LAW", 3.0);
                addCourse(ps, "LAW1201", "PDR, Registration and Court Fees Act", "LAW", 3.0);
                addCourse(ps, "LAW1202", "Muslim Law", "LAW", 3.0);
                addCourse(ps, "CSC1201", "Introduction to Computer", "LAW", 3.0);
                addCourse(ps, "LAW1203", "Constitutional Law of Bangladesh", "LAW", 3.0);
                addCourse(ps, "LAW1204", "Law of Agency, Bailment & Partnership", "LAW", 3.0);
                addCourse(ps, "BUS2101", "Introduction to Business", "LAW", 3.0);
                addCourse(ps, "LAW2101", "Constitutional Law of U.K. & U.S.A.", "LAW", 3.0);
                addCourse(ps, "LAW2102", "Hindu Law", "LAW", 3.0);
                addCourse(ps, "BAS2102", "Bangladesh Studies", "LAW", 3.0);
                addCourse(ps, "LAW2103", "Law of Torts", "LAW", 3.0);
                addCourse(ps, "LAW2104", "Law of Equity and Trust", "LAW", 3.0);
                addCourse(ps, "POL2201", "Government and Politics", "LAW", 3.0);
                addCourse(ps, "LAW2201", "Law of Taxation", "LAW", 3.0);
                addCourse(ps, "LAW2202", "Labor and Industrial Law", "LAW", 3.0);
                addCourse(ps, "LAW2203", "Mercantile Law", "LAW", 3.0);
                addCourse(ps, "LAW2204", "Law of Transfer of Property", "LAW", 3.0);
                addCourse(ps, "LAW2205", "E-Commerce and E-Governance", "LAW", 3.0);
                addCourse(ps, "LAW3101", "Public International Law", "LAW", 3.0);
                addCourse(ps, "LAW3102", "Company Law", "LAW", 3.0);
                addCourse(ps, "LAW3103", "Specific Relief Act and Law of Limitation", "LAW", 3.0);
                addCourse(ps, "LAW3104", "Criminology and Law of Cyber Crimes", "LAW", 3.0);
                addCourse(ps, "LAW3105", "International Trade Law", "LAW", 3.0);
                addCourse(ps, "LAW3106", "General Clauses Act & Interpretation of Statutes", "LAW", 3.0);
                addCourse(ps, "LAW3201", "Law of Evidence", "LAW", 3.0);
                addCourse(ps, "LAW3202", "Law of Crimes", "LAW", 3.0);
                addCourse(ps, "LAW3203", "Jurisdiction in International Law", "LAW", 3.0);
                addCourse(ps, "LAW3204", "Code of Civil Procedure I", "LAW", 3.0);
                addCourse(ps, "LAW3205", "Code of Criminal Procedure I", "LAW", 3.0);
                addCourse(ps, "LAW3206", "Administrative Law", "LAW", 3.0);
                addCourse(ps, "LAW4101", "Land Laws of Bangladesh", "LAW", 3.0);
                addCourse(ps, "LAW4102", "Drafting and Conveyance and Pleading", "LAW", 3.0);
                addCourse(ps, "LAW4103", "Code of Civil Procedure II", "LAW", 3.0);
                addCourse(ps, "LAW4104", "Code of Criminal Procedure II", "LAW", 3.0);
                addCourse(ps, "LAW4105", "Alternative Dispute Resolution and Trial Advocacy", "LAW", 3.0);
                addCourse(ps, "LAW4106", "Moot Court, Mock Trial & Practice", "LAW", 3.0);
                addCourse(ps, "LAW4201", "Intellectual Property Law", "LAW", 3.0);
                addCourse(ps, "LAW4202", "Professional Orientation", "LAW", 3.0);
                addCourse(ps, "LAW4299", "Research Monograph", "LAW", 3.0);

                // PHARMACY
                addCourse(ps, "PHR1101", "Computer Fundamentals", "PHARMACY", 3.0);
                addCourse(ps, "ENG1104", "English Reading Skills & Public Speaking", "PHARMACY", 3.0);
                addCourse(ps, "BAS1102", "Bangladesh Studies", "PHARMACY", 3.0);
                addCourse(ps, "PHR1102", "Introduction to Pharmacy", "PHARMACY", 3.0);
                addCourse(ps, "PHR1201", "Pharmacognosy & Nutraceuticals and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR1202", "Human Anatomy & Physiology I", "PHARMACY", 3.0);
                addCourse(ps, "PHR1203", "Inorganic Pharmacy I and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR2101", "Organic Pharmacy I and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR2102", "Physical Pharmacy and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR2103", "Pharmaceutical Statistics", "PHARMACY", 3.0);
                addCourse(ps, "PHR2100", "Viva Voce 1", "PHARMACY", 1.0);
                addCourse(ps, "PHR2201", "Inorganic Pharmacy II and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR2202", "Organic Pharmacy II and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR2203", "Human Anatomy & Physiology II and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR3101", "Pharmaceutical Microbiology I and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR3102", "Pharmacology I", "PHARMACY", 3.0);
                addCourse(ps, "PHR3103", "Human Anatomy & Physiology III and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR3104", "GMP & Pharmaceutical Industrial Practice", "PHARMACY", 3.0);
                addCourse(ps, "PHR3201", "Pharmaceutical Microbiology II and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR3202", "Pharmaceutical Dosage Form I and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR3203", "Biochemistry and Molecular Biology and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR3200", "Viva Voce 2", "PHARMACY", 1.0);
                addCourse(ps, "PHR3204", "Pharmaceutical Engineering I", "PHARMACY", 3.0);
                addCourse(ps, "PHR4101", "Pharmaceutical Dosage Form II and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR4102", "Clinical Pharmacy", "PHARMACY", 3.0);
                addCourse(ps, "PHR4103", "Pharmacy Practice I", "PHARMACY", 3.0);
                addCourse(ps, "PHR4104", "Pharmaceutical Analysis I and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR4105", "Pharmacology II and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR4201", "Pharmaceutical Dosage Form III and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR4202", "Pharmacology III and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR4203", "Medicinal Chemistry I", "PHARMACY", 3.0);
                addCourse(ps, "PHR4204", "Pharmaceutical Analysis II and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR4205", "Pharmaceutical Engineering II", "PHARMACY", 3.0);
                addCourse(ps, "PHR4206", "Research Methodology", "PHARMACY", 3.0);
                addCourse(ps, "PHR4200", "Viva Voce 3", "PHARMACY", 1.0);
                addCourse(ps, "PHR4301", "Biopharmaceutics & Clinical Pharmacokinetic I", "PHARMACY", 3.0);
                addCourse(ps, "PHR4302", "Pharmaceutical Marketing & Management", "PHARMACY", 3.0);
                addCourse(ps, "PHR4303", "Pharmacology IV", "PHARMACY", 3.0);
                addCourse(ps, "PHR4304", "Advanced Pharmaceutical Analysis", "PHARMACY", 3.0);
                addCourse(ps, "PHR4305", "Medicinal Chemistry II and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR4306", "Cosmetology and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR4307", "Pharmaceutical Industrial Training", "PHARMACY", 3.0);
                addCourse(ps, "PHR4300", "Viva Voce 4", "PHARMACY", 1.0);
                addCourse(ps, "PHR4401", "Basic Pathology & Toxicology and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR4402", "Biopharmaceutics & Clinical Pharmacokinetic II and Lab", "PHARMACY", 4.0);
                addCourse(ps, "PHR4403", "Pharmaceutical Regulatory Affairs", "PHARMACY", 3.0);
                addCourse(ps, "PHR4404", "Pharmacy Practice II", "PHARMACY", 3.0);
                addCourse(ps, "PHR4405", "Pharmaceutical Biotechnology", "PHARMACY", 3.0);
                addCourse(ps, "PHR4499", "Project & Dissertation", "PHARMACY", 3.0);

                // ENGLISH
                addCourse(ps, "ENG1101", "Computer Fundamentals", "ENGLISH", 3.0);
                addCourse(ps, "BAN1101", "Bangla Language and Literature", "ENGLISH", 3.0);
                addCourse(ps, "ENG1102", "Introduction to Linguistics", "ENGLISH", 3.0);
                addCourse(ps, "SOC1101", "Foundations of Sociology", "ENGLISH", 3.0);
                addCourse(ps, "ENG1105", "English Reading Skills & Public Speaking", "ENGLISH", 3.0);
                addCourse(ps, "ENG1106", "Introduction to English Literature", "ENGLISH", 3.0);
                addCourse(ps, "SCI1101", "Natural Science", "ENGLISH", 3.0);
                addCourse(ps, "BAS1201", "History of Emergence of Bangladesh", "ENGLISH", 3.0);
                addCourse(ps, "ECO1201", "Fundamentals of Economics", "ENGLISH", 3.0);
                addCourse(ps, "ENG1207", "English Writing", "ENGLISH", 3.0);
                addCourse(ps, "ENG1208", "Morphology", "ENGLISH", 3.0);
                addCourse(ps, "ENG1209", "Appreciation of Poetry", "ENGLISH", 3.0);
                addCourse(ps, "ENG2104", "Writing for Arts and Social Sciences", "ENGLISH", 3.0);
                addCourse(ps, "ART2101", "Arts and Aesthetics", "ENGLISH", 3.0);
                addCourse(ps, "ENG2105", "History of English Language", "ENGLISH", 3.0);
                addCourse(ps, "STA2101", "Statistics for Social Science", "ENGLISH", 3.0);
                addCourse(ps, "ACT2101", "Elementary Accounting", "ENGLISH", 3.0);
                addCourse(ps, "BAS2103", "Bangladesh Studies", "ENGLISH", 3.0);
                addCourse(ps, "SOC2201", "Social Science Research Methodology", "ENGLISH", 3.0);
                addCourse(ps, "ENG2204", "Professional English", "ENGLISH", 3.0);
                addCourse(ps, "ENG2205", "Appreciation of Drama", "ENGLISH", 3.0);
                addCourse(ps, "PSY2201", "Cognitive and Social Psychology", "ENGLISH", 3.0);
                addCourse(ps, "ENG2206", "Appreciation of Prose", "ENGLISH", 3.0);
                addCourse(ps, "ELT2201", "Classroom Management Techniques in ELT", "ENGLISH", 3.0);

                // BBA
                addCourse(ps, "BBA1101", "Introduction to Business", "BBA", 3.0);
                addCourse(ps, "ACT1101", "Principles of Accounting", "BBA", 3.0);
                addCourse(ps, "MGT1101", "Principles of Management", "BBA", 3.0);
                addCourse(ps, "MKT1101", "Principles of Marketing", "BBA", 3.0);
                addCourse(ps, "ECO1101", "Microeconomics", "BBA", 3.0);
                addCourse(ps, "ECO1202", "Macroeconomics", "BBA", 3.0);
                addCourse(ps, "MAT1103", "Business Mathematics", "BBA", 3.0);
                addCourse(ps, "STA1201", "Business Statistics", "BBA", 3.0);
                addCourse(ps, "ENG1205", "Business Communication", "BBA", 3.0);
                addCourse(ps, "FIN2101", "Financial Management", "BBA", 3.0);
                addCourse(ps, "HRM2101", "Human Resource Management", "BBA", 3.0);
                addCourse(ps, "LAW2105", "Business Law", "BBA", 3.0);
                addCourse(ps, "MIS2201", "Management Information Systems", "BBA", 3.0);
                addCourse(ps, "OSCM2201", "Operations Management", "BBA", 3.0);
                addCourse(ps, "MGT3102", "Strategic Management", "BBA", 3.0);
                addCourse(ps, "IB3101", "International Business", "BBA", 3.0);
                addCourse(ps, "MGT3103", "Organizational Behavior", "BBA", 3.0);
                addCourse(ps, "ECOM3201", "E-Commerce", "BBA", 3.0);
                addCourse(ps, "ETH3201", "Business Ethics", "BBA", 3.0);
                addCourse(ps, "BBA4299", "Internship / Project", "BBA", 3.0);

                // CHEMISTRY
                addCourse(ps, "CHE1101", "Chemistry (CSE Foundation)", "CHEMISTRY", 3.0);
                addCourse(ps, "CHE1102", "General Chemistry", "CHEMISTRY", 3.0);
                addCourse(ps, "CHE1201", "Organic Chemistry", "CHEMISTRY", 3.0);
                addCourse(ps, "CHE1202", "Inorganic Chemistry", "CHEMISTRY", 3.0);
                addCourse(ps, "CHE2101", "Physical Chemistry", "CHEMISTRY", 3.0);
                addCourse(ps, "CHE2102", "Analytical Chemistry", "CHEMISTRY", 3.0);
                addCourse(ps, "CHE2103", "Chemistry Lab", "CHEMISTRY", 1.0);
                addCourse(ps, "CHE2201", "Environmental Chemistry", "CHEMISTRY", 3.0);
                addCourse(ps, "CHE2202", "Industrial Chemistry", "CHEMISTRY", 3.0);
                addCourse(ps, "CHE3101", "Biochemistry", "CHEMISTRY", 3.0);

                ps.executeBatch();
            }
        } catch (SQLException e) {
            System.err.println("Error initializing courses/results schema: " + e.getMessage());
        }
    }

    private static void addCourse(PreparedStatement ps, String code, String name, String dept, double cr) throws SQLException {
        ps.setString(1, code);
        ps.setString(2, name);
        ps.setString(3, dept);
        ps.setDouble(4, cr);
        ps.addBatch();
    }
}
