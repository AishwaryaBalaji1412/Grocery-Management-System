
package com.grocery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public final class DatabaseConnection {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private static final String URL = getConfig(
            "DB_URL",
            "jdbc:mysql://localhost:3306/grocery_management"
    );

    private static final String USERNAME = getConfig(
            "DB_USERNAME",
            "root"
    );

    private static final String PASSWORD = getConfig(
            "DB_PASSWORD",
            ""
    );

    private DatabaseConnection() {
        // Prevent object creation.
    }

    private static String getConfig(String key, String defaultValue) {

        String environmentValue = System.getenv(key);

        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        String dotenvValue = DOTENV.get(key);

        if (dotenvValue != null && !dotenvValue.isBlank()) {
            return dotenvValue.trim();
        }

        return defaultValue;
    }

    public static Connection getConnection() throws SQLException {

        if (URL == null || !URL.startsWith("jdbc:mysql://")) {
            throw new SQLException("Invalid MySQL database URL.");
        }

        if (USERNAME == null || USERNAME.isBlank()) {
            throw new SQLException("Database username is missing.");
        }

        try {
            return DriverManager.getConnection(
                    URL,
                    USERNAME,
                    PASSWORD
            );

        } catch (SQLException ex) {
            throw new SQLException(
                    "Unable to connect to MySQL. Check that MySQL is running "
                    + "and your database credentials are correct.",
                    ex
            );
        }
    }
}
