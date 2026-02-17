package org.sam.projectmanager.techy_pma.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles SQLite database connection and initial table setup.
 *
 * Provides a shared connection and ensures required tables exist
 * when the application starts.
 */
public class DatabaseManager {

    // SQLite database file location
    private static final String DATABASE_URL = "jdbc:sqlite:data/projectmanager.db";

    // Shared connection instance
    private static Connection connection;

    /**
     * Returns an active database connection.
     * Creates a new connection if none exists or if it was closed.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DATABASE_URL);
        }
        return connection;
    }

    /**
     * Creates application tables if they do not already exist.
     * Should be called once during application startup.
     */
    public static void initializeDatabase() {

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Projects table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS projects (
                    project_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    project_name TEXT NOT NULL,
                    project_description TEXT,
                    project_progress INTEGER DEFAULT 0,
                    created_by INTEGER NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    status TEXT DEFAULT 'not started'
                        CHECK(status IN ('not started', 'in progress', 'completed', 'published')),
                    FOREIGN KEY (created_by) REFERENCES users(user_id)
                )
            """);

            // Project members (many-to-many relationship)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS project_members (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    project_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    role TEXT NOT NULL,
                    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (project_id) REFERENCES projects(project_id),
                    FOREIGN KEY (user_id) REFERENCES users(user_id),
                    UNIQUE(project_id, user_id)
                )
            """);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Closes the database connection if it is open.
     * Should be called on application shutdown.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
