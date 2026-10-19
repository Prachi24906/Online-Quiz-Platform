package com.sem3.quiz;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Clean login screen for Admin, Quiz Creator and Participant accounts. */
public class LoginFrame extends JFrame {
    // Keep the fields empty so credentials are never filled in automatically.
    private final JTextField email = new JTextField(22);
    private final JPasswordField password = new JPasswordField(22);
    private final QuizDAO dao = new QuizDAO();

    private final Color blue = new Color(37, 99, 235);
    private final Color ink = new Color(30, 41, 59);
    private final Color muted = new Color(100, 116, 139);

    public LoginFrame() {
        setTitle("Java Quiz Platform | Sign in");
        setSize(540, 570);
        setMinimumSize(new Dimension(500, 540));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(241, 245, 249));

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            new EmptyBorder(28, 34, 28, 34)));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        g.insets = new Insets(7, 4, 7, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        JLabel title = new JLabel("Java Quiz Platform", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        title.setForeground(blue);
        card.add(title, g);

        g.gridy++;
        JLabel subtitle = new JLabel("Sign in to your account", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(muted);
        card.add(subtitle, g);

        g.gridy++;
        g.insets = new Insets(18, 4, 4, 4);
        JLabel emailLabel = new JLabel("Email address");
        styleLabel(emailLabel);
        card.add(emailLabel, g);

        g.gridy++; g.insets = new Insets(4, 4, 10, 4);
        styleField(email);
        card.add(email, g);

        g.gridy++; g.insets = new Insets(8, 4, 4, 4);
        JLabel passwordLabel = new JLabel("Password");
        styleLabel(passwordLabel);
        card.add(passwordLabel, g);

        g.gridy++; g.insets = new Insets(4, 4, 16, 4);
        styleField(password);
        card.add(password, g);

        g.gridy++; g.insets = new Insets(5, 4, 8, 4);
        JButton login = new JButton("SIGN IN");
        login.setFont(new Font("SansSerif", Font.BOLD, 15));
        login.setBackground(blue);
        login.setForeground(Color.WHITE);
        login.setOpaque(true);
        login.setBorderPainted(false);
        login.setFocusPainted(false);
        login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        login.setPreferredSize(new Dimension(300, 44));
        login.addActionListener(e -> doLogin());
        card.add(login, g);

        g.gridy++; g.insets = new Insets(10, 4, 2, 4);
        JLabel registerText = new JLabel("New participant? Create an account below.", SwingConstants.CENTER);
        registerText.setForeground(muted);
        registerText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        card.add(registerText, g);

        g.gridy++; g.insets = new Insets(2, 4, 8, 4);
        JButton register = new JButton("CREATE PARTICIPANT ACCOUNT");
        register.setFont(new Font("SansSerif", Font.BOLD, 13));
        register.setForeground(blue);
        register.setBackground(new Color(239, 246, 255));
        register.setOpaque(true);
        register.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254)));
        register.setFocusPainted(false);
        register.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        register.setPreferredSize(new Dimension(300, 42));
        register.addActionListener(e -> showRegistration());
        card.add(register, g);

        g.gridy++; g.insets = new Insets(10, 4, 0, 4);
        JLabel note = new JLabel("Demo accounts are listed in the project README.", SwingConstants.CENTER);
        note.setForeground(muted);
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        card.add(note, g);

        outer.add(card);
        setContentPane(outer);
        getRootPane().setDefaultButton(login);
    }

    private void styleLabel(JLabel label) {
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(ink);
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(330, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225)),
            new EmptyBorder(7, 10, 7, 10)));
    }

    private void doLogin() {
        String enteredEmail = email.getText().trim();
        String enteredPassword = new String(password.getPassword());
        if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both your email and password.", "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            User user = dao.login(enteredEmail, enteredPassword);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Login failed. Check your email and password.", "Sign-in failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dispose();
            new DashboardFrame(user).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not connect to MySQL. Check db.properties and the database service.\n\n" + ex.getMessage(),
                "Database connection error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegistration() {
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.add(new JLabel("Full name")); form.add(nameField);
        form.add(new JLabel("Email address")); form.add(emailField);
        form.add(new JLabel("Password (at least 6 characters)")); form.add(passwordField);
        form.add(new JLabel("Confirm password")); form.add(confirmField);

        int choice = JOptionPane.showConfirmDialog(this, form, "Create Participant Account",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPassword = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmField.getPassword());

        if (name.isEmpty() || newEmail.isEmpty() || newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in every field.", "Missing details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.", "Invalid email", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "Your password must contain at least 6 characters.", "Password too short", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "The passwords do not match.", "Check password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            dao.registerParticipant(name, newEmail, newPassword);
            JOptionPane.showMessageDialog(this, "Account created successfully! You can now sign in.", "Registration complete", JOptionPane.INFORMATION_MESSAGE);
            email.setText(newEmail);
            password.setText("");
            password.requestFocusInWindow();
        } catch (java.sql.SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "An account with that email already exists.", "Email already registered", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not create the account. Check the database connection.\n\n" + ex.getMessage(),
                "Registration error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
