package org.sam.projectmanager.techy_pma.database;

import org.sam.projectmanager.techy_pma.models.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * Insert a new user into the database
     * Returns true if successful, false otherwise
     */
    public static boolean insertUser(User user) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters (replace ? with actual values)
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());

            // Execute the insert
            int rowsAffected = stmt.executeUpdate();

            // Get the auto-generated user_id
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                }
                System.out.println("✓ User inserted successfully: " + user.getUsername());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error inserting user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get a user by their user_id
     * Returns User object if found, null otherwise
     */
    public static User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get a user by their username (for login)
     * Returns User object if found, null otherwise
     */
    public static User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting user by username: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get a user by their email
     * Returns User object if found, null otherwise
     */
    public static User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting user by email: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get all users from the database
     * Returns List of User objects
     */
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Update an existing user
     * Returns true if successful, false otherwise
     */
    public static boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password = ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setInt(4, user.getUserId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ User updated successfully: " + user.getUsername());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error updating user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Delete a user by their user_id
     * Returns true if successful, false otherwise
     */
    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ User deleted successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Check if a username already exists
     * Returns true if exists, false otherwise
     */
    public static boolean usernameExists(String username) {
        return getUserByUsername(username) != null;
    }

    /**
     * Check if an email already exists
     * Returns true if exists, false otherwise
     */
    public static boolean emailExists(String email) {
        return getUserByEmail(email) != null;
    }

    /**
     * Helper method to extract User object from ResultSet
     * Reduces code duplication
     */
    private static User extractUserFromResultSet(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");

        // Handle the timestamp - it might be null
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;

        return new User(userId, username, email, password, createdAt);
    }
}