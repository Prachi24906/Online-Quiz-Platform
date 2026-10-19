package com.sem3.quiz;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/** Opens MySQL connections using settings in src/main/resources/db.properties. */
public class DB {
    private static final Properties SETTINGS = new Properties();

    static {
        try (InputStream in = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new IllegalStateException("db.properties was not found.");
            SETTINGS.load(in);
        } catch (Exception ex) {
            throw new RuntimeException("Could not read database settings: " + ex.getMessage(), ex);
        }
    }

    public static Connection getConnection() throws Exception {
        // Keeping connection details in a properties file avoids hard-coding them in every class.
        return DriverManager.getConnection(
            SETTINGS.getProperty("db.url"),
            SETTINGS.getProperty("db.user"),
            SETTINGS.getProperty("db.password")
        );
    }
}
