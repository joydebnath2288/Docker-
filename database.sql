CREATE DATABASE IF NOT EXISTS student_management;

USE student_management;

CREATE TABLE IF NOT EXISTS roles (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

INSERT IGNORE INTO roles (id, name, display_name, description) VALUES
(1, 'ADMIN', 'Administrator', 'Full system access, student deletion, and configuration'),
(2, 'STUDENT', 'Student', 'Personal academic record and published results'),
(3, 'FACULTY', 'Faculty', 'View students, assign courses, evaluate and enter course marks'),
(4, 'REGISTRAR', 'Registrar', 'Student admissions, add and edit student records, course enrollment'),
(5, 'ACCOUNTS', 'Accounts', 'View student directory and enrollment records for tuition and fees'),
(6, 'SUPERVISOR', 'Supervisor', 'Academic oversight, review results, and approve result publication');

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role INT NOT NULL DEFAULT 2,
    status INT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS students (
    id INT PRIMARY KEY,
    uid VARCHAR(36) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    department VARCHAR(50) NOT NULL,
    cgpa DECIMAL(3, 2) NOT NULL,
    semester VARCHAR(50) NOT NULL DEFAULT 'FALL 2025',
    CONSTRAINT fk_students_users FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(30) NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    department VARCHAR(50) NOT NULL,
    credit_hours DECIMAL(3, 1) NOT NULL DEFAULT 3.0,
    CONSTRAINT uq_course_dept UNIQUE (course_code, department)
);

CREATE TABLE IF NOT EXISTS student_results (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    semester VARCHAR(50) NOT NULL DEFAULT 'FALL 2025',
    marks DECIMAL(5, 2) DEFAULT NULL,
    grade_point DECIMAL(3, 2) NOT NULL DEFAULT 0.00,
    letter_grade VARCHAR(5) DEFAULT '-',
    published BOOLEAN NOT NULL DEFAULT FALSE,
    evaluated BOOLEAN NOT NULL DEFAULT FALSE,
    enrolled_by VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_result_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_result_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uq_student_course UNIQUE (student_id, course_id)
);
