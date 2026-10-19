# Java-Based Online Quiz Platform — Semester 3

A Java Swing desktop application using Maven, JDBC and MySQL. The interface uses a clean white/light-blue theme and the source includes beginner-friendly comments.

## Implemented features

### Admin dashboard
- View registered accounts and add, edit or delete users (database foreign-key rules may prevent deleting accounts already linked to quiz history).
- Review submitted quizzes and approve or reject them.
- View recorded quiz attempts and scores.
- Save the platform name and default quiz duration.
- View system operation reminders/alerts.

### Quiz Creator dashboard
- Create multiple-choice quizzes with a title, description, duration and 1–20 questions.
- Each question has four options and a selected correct answer.
- New quizzes are saved as `PENDING` until an admin approves them.
- View quiz history/status and recorded participant results.
- Read and send messages to other users.

### Participant dashboard
- Create a participant account from the sign-in screen; login fields start empty.
- View and take admin-approved quizzes with a countdown timer.
- See the score, percentage and question-by-question answer feedback after submission.
- Review previous results, view the leaderboard, message other users and save quiz reminders.

### Technical features
- Java Swing GUI, object-oriented classes (`User`, `Quiz`, `Question`), Java collections, JDBC `PreparedStatement`, and a scheduled countdown timer.
- Database connection values are in `src/main/resources/db.properties`.
- Database schema and sample data are in `database/quiz_platform.sql`.

## Requirements
- JDK 17 or newer
- Maven 3.9 or newer
- MySQL Server
- VS Code with the Java Extension Pack (or another Java IDE)

## Setup

1. Extract the ZIP file.
2. Start the MySQL service.
3. Open a terminal in the extracted `JavaQuizPlatform` folder.
4. Run the SQL script to create any missing tables, including the reminders table:
   ```powershell
   Get-Content .\database\quiz_platform.sql | mysql -u root -p
   ```
   The script uses `CREATE TABLE IF NOT EXISTS` and does not drop existing tables. It seeds the sample users and sample quiz only if they are not already present.
5. Open `src/main/resources/db.properties` and replace `CHANGE_ME` with your MySQL root password. Do not share that password.
6. Compile and launch:
   ```powershell
   mvn clean compile
   mvn exec:java
   ```

## Demo accounts

| Role | Email | Password |
|---|---|---|
| Admin | `admin@quizplatform.com` | `admin123` |
| Quiz Creator | `creator@quizplatform.com` | `creator123` |
| Participant | `participant@example.com` | `quiz123` |

These are sample database records. The sign-in form does not pre-fill any credentials. New public registrations are participant accounts; admins can add creator/admin accounts from User Management.

## Suggested demonstration flow

1. Sign in as creator and create a quiz with a few questions.
2. Sign out, sign in as admin and approve it in Quiz Approval.
3. Sign out, sign in as participant and take the quiz.
4. Review the score and detailed answer feedback, then check My Performance and Leaderboard.
5. Demonstrate participant reminders and messaging.

## Classroom-project security note

This is a student demonstration project, not production authentication. For simplicity, passwords are stored as plain text in the database. A real deployed application should use password hashing, stronger validation and access controls. Do not use real or reused passwords.
