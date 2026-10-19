package com.sem3.quiz;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Starts the desktop application. */
public class Main {
    public static void main(String[] args) {
        // Use the system's normal window style where available.
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { /* The default Swing style is still usable. */ }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
