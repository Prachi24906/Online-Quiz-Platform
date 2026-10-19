-- Java-Based Online Quiz Platform
-- Run this file in MySQL Workbench or with: mysql -u root -p < database/quiz_platform.sql

CREATE DATABASE IF NOT EXISTS java_quiz_platform;
USE java_quiz_platform;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('ADMIN','CREATOR','PARTICIPANT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS quizzes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    duration_minutes INT NOT NULL DEFAULT 10,
    creator_id INT NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    question_text VARCHAR(500) NOT NULL,
    option_a VARCHAR(250) NOT NULL,
    option_b VARCHAR(250) NOT NULL,
    option_c VARCHAR(250) NOT NULL,
    option_d VARCHAR(250) NOT NULL,
    correct_option CHAR(1) NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attempts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    participant_id INT NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    time_taken_seconds INT NOT NULL DEFAULT 0,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    FOREIGN KEY (participant_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    message_text VARCHAR(1000) NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS settings (
    setting_key VARCHAR(80) PRIMARY KEY,
    setting_value VARCHAR(250) NOT NULL
);

CREATE TABLE IF NOT EXISTS quiz_reminders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    participant_id INT NOT NULL,
    reminder_text VARCHAR(300) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (participant_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT IGNORE INTO users(name,email,password,role) VALUES
('System Admin','admin@quizplatform.com','admin123','ADMIN'),
('Quiz Creator','creator@quizplatform.com','creator123','CREATOR'),
('Aarav Sharma','participant@example.com','quiz123','PARTICIPANT');

INSERT IGNORE INTO settings(setting_key,setting_value) VALUES
('platform_name','Java Quiz Platform'),
('default_duration_minutes','10');

-- Sample approved quiz and questions. Seeded only when the database is first set up.
INSERT INTO quizzes(title,description,duration_minutes,creator_id,status)
SELECT 'Java Basics','Starter quiz covering beginner Java concepts.',5,u.id,'APPROVED'
FROM users u WHERE u.email='creator@quizplatform.com'
AND NOT EXISTS (SELECT 1 FROM quizzes WHERE title='Java Basics');

SET @sample_quiz_id = (SELECT id FROM quizzes WHERE title='Java Basics' LIMIT 1);
INSERT INTO questions(quiz_id,question_text,option_a,option_b,option_c,option_d,correct_option)
SELECT @sample_quiz_id,'Which keyword creates a class in Java?','define','class','struct','new','B'
WHERE @sample_quiz_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM questions WHERE quiz_id=@sample_quiz_id);
INSERT INTO questions(quiz_id,question_text,option_a,option_b,option_c,option_d,correct_option)
SELECT @sample_quiz_id,'Which method is the usual entry point of a Java program?','start()','run()','main()','init()','C'
WHERE @sample_quiz_id IS NOT NULL AND (SELECT COUNT(*) FROM questions WHERE quiz_id=@sample_quiz_id)<2;
INSERT INTO questions(quiz_id,question_text,option_a,option_b,option_c,option_d,correct_option)
SELECT @sample_quiz_id,'Which type stores true or false?','int','String','char','boolean','D'
WHERE @sample_quiz_id IS NOT NULL AND (SELECT COUNT(*) FROM questions WHERE quiz_id=@sample_quiz_id)<3;
