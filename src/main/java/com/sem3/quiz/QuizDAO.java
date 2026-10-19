package com.sem3.quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database operations for quizzes, questions, user accounts and results.
 * PreparedStatement is used for values supplied by users.
 */
public class QuizDAO {
    public User login(String email, String password) throws Exception {
        String sql = "SELECT id,name,email,role FROM users WHERE email=? AND password=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, email); p.setString(2, password);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) return new User(r.getInt("id"), r.getString("name"), r.getString("email"), r.getString("role"));
            }
        }
        return null;
    }

    public List<Quiz> getQuizzes(String role, int userId, String status) throws Exception {
        List<Quiz> list = new ArrayList<>();
        String sql;
        if ("CREATOR".equals(role)) {
            sql = "SELECT id,title,description,duration_minutes,status FROM quizzes WHERE creator_id=? ORDER BY id DESC";
        } else if ("ADMIN".equals(role) && status != null) {
            sql = "SELECT id,title,description,duration_minutes,status FROM quizzes WHERE status=? ORDER BY id DESC";
        } else {
            sql = "SELECT id,title,description,duration_minutes,status FROM quizzes WHERE status='APPROVED' ORDER BY id DESC";
        }
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            if ("CREATOR".equals(role)) p.setInt(1, userId);
            else if ("ADMIN".equals(role) && status != null) p.setString(1, status);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(new Quiz(r.getInt("id"), r.getString("title"),
                    r.getString("description"), r.getInt("duration_minutes"), r.getString("status")));
            }
        }
        return list;
    }

    public List<Question> getQuestions(int quizId) throws Exception {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE quiz_id=? ORDER BY id";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, quizId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(new Question(r.getInt("id"), r.getString("question_text"),
                    r.getString("option_a"), r.getString("option_b"), r.getString("option_c"),
                    r.getString("option_d"), r.getString("correct_option")));
            }
        }
        return list;
    }

    public int createQuiz(String title, String description, int duration, int creatorId,
                          List<Question> questions) throws Exception {
        String quizSql = "INSERT INTO quizzes(title,description,duration_minutes,creator_id,status) VALUES(?,?,?,?, 'PENDING')";
        String questionSql = "INSERT INTO questions(quiz_id,question_text,option_a,option_b,option_c,option_d,correct_option) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DB.getConnection()) {
            c.setAutoCommit(false);
            try {
                int quizId;
                try (PreparedStatement p = c.prepareStatement(quizSql, Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1,title); p.setString(2,description); p.setInt(3,duration); p.setInt(4,creatorId);
                    p.executeUpdate();
                    try (ResultSet keys = p.getGeneratedKeys()) { keys.next(); quizId = keys.getInt(1); }
                }
                try (PreparedStatement p = c.prepareStatement(questionSql)) {
                    for (Question q : questions) {
                        p.setInt(1,quizId); p.setString(2,q.getText()); p.setString(3,q.getA());
                        p.setString(4,q.getB()); p.setString(5,q.getC()); p.setString(6,q.getD()); p.setString(7,q.getCorrect());
                        p.addBatch();
                    }
                    p.executeBatch();
                }
                c.commit();
                return quizId;
            } catch (Exception ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    public void updateQuizStatus(int quizId, String status) throws Exception {
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE quizzes SET status=? WHERE id=?")) {
            p.setString(1,status); p.setInt(2,quizId); p.executeUpdate();
        }
    }

    /** Public registration is limited to participant accounts. */
    public void registerParticipant(String name, String email, String password) throws Exception {
        String sql = "INSERT INTO users(name,email,password,role) VALUES(?,?,?,'PARTICIPANT')";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, name);
            p.setString(2, email);
            p.setString(3, password);
            p.executeUpdate();
        }
    }

    public void addUser(String name, String email, String password, String role) throws Exception {
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(
            "INSERT INTO users(name,email,password,role) VALUES(?,?,?,?)")) {
            p.setString(1,name); p.setString(2,email); p.setString(3,password); p.setString(4,role); p.executeUpdate();
        }
    }

    public List<String[]> getUsers() throws Exception {
        List<String[]> list = new ArrayList<>();
        try (Connection c = DB.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT id,name,email,role FROM users ORDER BY id")) {
            while (r.next()) list.add(new String[]{String.valueOf(r.getInt(1)),r.getString(2),r.getString(3),r.getString(4)});
        }
        return list;
    }


    public void updateUser(int id, String name, String email, String password, String role) throws Exception {
        String sql = password == null || password.isBlank()
            ? "UPDATE users SET name=?,email=?,role=? WHERE id=?"
            : "UPDATE users SET name=?,email=?,password=?,role=? WHERE id=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, name); p.setString(2, email);
            if (password == null || password.isBlank()) {
                p.setString(3, role); p.setInt(4, id);
            } else {
                p.setString(3, password); p.setString(4, role); p.setInt(5, id);
            }
            p.executeUpdate();
        }
    }

    public void deleteUser(int id) throws Exception {
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM users WHERE id=?")) {
            p.setInt(1, id);
            p.executeUpdate();
        }
    }

    
public void addReminder(int participantId, String reminderText) throws Exception {
    try (Connection c = DB.getConnection();
         PreparedStatement p = c.prepareStatement(
             "INSERT INTO quiz_reminders (user_id, quiz_id, reminder_time) VALUES (?, ?, ?)")) {

        p.setInt(1, participantId);

        try (PreparedStatement quizQuery = c.prepareStatement(
                 "SELECT id FROM quizzes ORDER BY id LIMIT 1");
             ResultSet rs = quizQuery.executeQuery()) {

            if (!rs.next()) {
                throw new Exception("No quizzes are available for reminders.");
            }

            p.setInt(2, rs.getInt("id"));
        }

        p.setTimestamp(3, java.sql.Timestamp.valueOf(
            java.time.LocalDateTime.now().plusDays(1)));

        p.executeUpdate();
    }
}


    
public List<String[]> getReminders(int participantId) throws Exception {
    List<String[]> list = new ArrayList<>();

    String sql = "SELECT qr.id, q.title, qr.reminder_time " +
                 "FROM quiz_reminders qr " +
                 "JOIN quizzes q ON qr.quiz_id = q.id " +
                 "WHERE qr.user_id = ? " +
                 "ORDER BY qr.reminder_time DESC";

    try (Connection c = DB.getConnection();
         PreparedStatement p = c.prepareStatement(sql)) {

        p.setInt(1, participantId);

        try (ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(new String[] {
                    r.getString("title"),
                    r.getString("reminder_time")
                });
            }
        }
    }

    return list;
}


    public void saveAttempt(int quizId, int participantId, int score, int total, int seconds) throws Exception {
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(
            "INSERT INTO attempts(quiz_id,participant_id,score,total_questions,time_taken_seconds) VALUES(?,?,?,?,?)")) {
            p.setInt(1,quizId); p.setInt(2,participantId); p.setInt(3,score); p.setInt(4,total); p.setInt(5,seconds); p.executeUpdate();
        }
    }

    public List<String[]> getAttempts(int userId, boolean allUsers) throws Exception {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT a.id,q.title,u.name,a.score,a.total_questions,a.attempted_at FROM attempts a " +
            "JOIN quizzes q ON a.quiz_id=q.id JOIN users u ON a.participant_id=u.id " +
            (allUsers ? "" : "WHERE a.participant_id=? ") + "ORDER BY a.attempted_at DESC";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            if (!allUsers) p.setInt(1,userId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(new String[]{r.getString("title"),r.getString("name"),
                    r.getString("score") + "/" + r.getString("total_questions"),r.getString("attempted_at")});
            }
        }
        return list;
    }

    public List<String[]> getLeaderboard() throws Exception {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT u.name, SUM(a.score) points, SUM(a.total_questions) total FROM attempts a " +
            "JOIN users u ON a.participant_id=u.id GROUP BY u.id,u.name ORDER BY points DESC";
        try (Connection c = DB.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) list.add(new String[]{r.getString("name"),r.getString("points"),r.getString("total")});
        }
        return list;
    }

    public void sendMessage(int sender, int receiver, String message) throws Exception {
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(
            "INSERT INTO messages(sender_id,receiver_id,message_text) VALUES(?,?,?)")) {
            p.setInt(1,sender); p.setInt(2,receiver); p.setString(3,message); p.executeUpdate();
        }
    }

    public List<String[]> getMessages(int userId) throws Exception {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT s.name sender,r.name receiver,m.message_text,m.sent_at FROM messages m " +
            "JOIN users s ON m.sender_id=s.id JOIN users r ON m.receiver_id=r.id " +
            "WHERE m.sender_id=? OR m.receiver_id=? ORDER BY m.sent_at DESC";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1,userId); p.setInt(2,userId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(new String[]{r.getString("sender"),r.getString("receiver"),r.getString("message_text"),r.getString("sent_at")});
            }
        }
        return list;
    }

    public void saveSetting(String key, String value) throws Exception {
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(
            "INSERT INTO settings(setting_key,setting_value) VALUES(?,?) ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)")) {
            p.setString(1,key); p.setString(2,value); p.executeUpdate();
        }
    }
}
