USE student_management;

-- 1. Ensure roles and users tables exist with correct schema
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

SET @dbname = DATABASE();

-- 2. Drop old fk_student_user (which referenced username) if it exists
SET @old_fk_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'students' AND CONSTRAINT_NAME = 'fk_student_user'
);
SET @sql_drop_old_fk = IF(@old_fk_exists > 0, 'ALTER TABLE students DROP FOREIGN KEY fk_student_user', 'SELECT 1');
PREPARE stmt_drop_old_fk FROM @sql_drop_old_fk;
EXECUTE stmt_drop_old_fk;
DEALLOCATE PREPARE stmt_drop_old_fk;

-- 3. Drop legacy username / password columns from students if they still exist
SET @has_pass_col = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'students' AND COLUMN_NAME = 'password'
);
SET @sql_drop_pass = IF(@has_pass_col > 0, 'ALTER TABLE students DROP COLUMN password', 'SELECT 1');
PREPARE stmt_drop_pass FROM @sql_drop_pass;
EXECUTE stmt_drop_pass;
DEALLOCATE PREPARE stmt_drop_pass;

SET @has_user_col = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'students' AND COLUMN_NAME = 'username'
);
SET @sql_drop_user = IF(@has_user_col > 0, 'ALTER TABLE students DROP COLUMN username', 'SELECT 1');
PREPARE stmt_drop_user FROM @sql_drop_user;
EXECUTE stmt_drop_user;
DEALLOCATE PREPARE stmt_drop_user;

-- 4. If user_id column exists, drop fk_student_user_id and uq_student_user_id
SET @fk_user_id_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'students' AND CONSTRAINT_NAME = 'fk_student_user_id'
);
SET @sql_drop_fk_uid = IF(@fk_user_id_exists > 0, 'ALTER TABLE students DROP FOREIGN KEY fk_student_user_id', 'SELECT 1');
PREPARE stmt_drop_fk_uid FROM @sql_drop_fk_uid;
EXECUTE stmt_drop_fk_uid;
DEALLOCATE PREPARE stmt_drop_fk_uid;

SET @uq_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'students' AND INDEX_NAME = 'uq_student_user_id'
);
SET @sql_drop_uq = IF(@uq_exists > 0, 'ALTER TABLE students DROP INDEX uq_student_user_id', 'SELECT 1');
PREPARE stmt_drop_uq FROM @sql_drop_uq;
EXECUTE stmt_drop_uq;
DEALLOCATE PREPARE stmt_drop_uq;

-- 5. Modify students.id to remove AUTO_INCREMENT
ALTER TABLE students MODIFY id INT NOT NULL;

-- 6. If user_id column exists, sync id = user_id and drop user_id column
SET @user_id_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'students' AND COLUMN_NAME = 'user_id'
);
SET @sql_sync_user_id = IF(@user_id_exists > 0, 'UPDATE students SET id = user_id WHERE user_id IS NOT NULL', 'SELECT 1');
PREPARE stmt_sync FROM @sql_sync_user_id;
EXECUTE stmt_sync;
DEALLOCATE PREPARE stmt_sync;

SET @sql_drop_col = IF(@user_id_exists > 0, 'ALTER TABLE students DROP COLUMN user_id', 'SELECT 1');
PREPARE stmt_drop_col FROM @sql_drop_col;
EXECUTE stmt_drop_col;
DEALLOCATE PREPARE stmt_drop_col;

-- 7. Add foreign key linking students.id directly to users.id
SET @fk_students_users_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'students' AND CONSTRAINT_NAME = 'fk_students_users'
);
SET @sql_add_fk = IF(
    @fk_students_users_exists = 0,
    'ALTER TABLE students ADD CONSTRAINT fk_students_users FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE',
    'SELECT 1'
);
PREPARE stmt_add_fk FROM @sql_add_fk;
EXECUTE stmt_add_fk;
DEALLOCATE PREPARE stmt_add_fk;

-- 8. Clean up orphaned user accounts
DELETE FROM users 
WHERE role = 2 
AND id NOT IN (SELECT id FROM students);

-- 9. Migrate student_results: add student_id (FK to students.id) and drop student_uid
SET @has_student_uid = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'student_results' AND COLUMN_NAME = 'student_uid'
);

SET @has_student_id = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'student_results' AND COLUMN_NAME = 'student_id'
);

-- Add student_id column if not exists
SET @sql_add_student_id = IF(@has_student_id = 0, 'ALTER TABLE student_results ADD COLUMN student_id INT AFTER id', 'SELECT 1');
PREPARE stmt_add_sid FROM @sql_add_student_id;
EXECUTE stmt_add_sid;
DEALLOCATE PREPARE stmt_add_sid;

-- Populate student_id by matching student_uid with students.uid
SET @sql_sync_sid = IF(@has_student_uid > 0, 'UPDATE student_results r JOIN students s ON r.student_uid = s.uid SET r.student_id = s.id', 'SELECT 1');
PREPARE stmt_sync_sid FROM @sql_sync_sid;
EXECUTE stmt_sync_sid;
DEALLOCATE PREPARE stmt_sync_sid;

-- Clean up any orphaned results
DELETE FROM student_results WHERE student_id IS NULL OR student_id <= 0;

-- Drop foreign key on student_uid if it exists
SET @fk_result_student_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'student_results' AND CONSTRAINT_NAME = 'fk_result_student'
);
SET @sql_drop_fk_res = IF(@fk_result_student_exists > 0, 'ALTER TABLE student_results DROP FOREIGN KEY fk_result_student', 'SELECT 1');
PREPARE stmt_drop_fk_res FROM @sql_drop_fk_res;
EXECUTE stmt_drop_fk_res;
DEALLOCATE PREPARE stmt_drop_fk_res;

-- Drop old unique constraint on student_uid, course_id
SET @uq_student_course_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'student_results' AND INDEX_NAME = 'uq_student_course'
);
SET @sql_drop_uq_res = IF(@uq_student_course_exists > 0, 'ALTER TABLE student_results DROP INDEX uq_student_course', 'SELECT 1');
PREPARE stmt_drop_uq_res FROM @sql_drop_uq_res;
EXECUTE stmt_drop_uq_res;
DEALLOCATE PREPARE stmt_drop_uq_res;

-- Drop student_uid column if it exists
SET @sql_drop_suid = IF(@has_student_uid > 0, 'ALTER TABLE student_results DROP COLUMN student_uid', 'SELECT 1');
PREPARE stmt_drop_suid FROM @sql_drop_suid;
EXECUTE stmt_drop_suid;
DEALLOCATE PREPARE stmt_drop_suid;

-- Make student_id NOT NULL
ALTER TABLE student_results MODIFY student_id INT NOT NULL;

-- Recreate foreign key and unique index referencing students(id)
ALTER TABLE student_results ADD CONSTRAINT fk_result_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;
ALTER TABLE student_results ADD CONSTRAINT uq_student_course UNIQUE (student_id, course_id);
