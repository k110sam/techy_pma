package org.sam.projectmanager.techy_pma.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a user in the application.
 * This class encapsulates user information including credentials and metadata.
 */
public class User {

    // Private fields
    private int userId;
    private String username;
    private String email;
    private String password; // hashed password
    private LocalDateTime createdAt;

    /**
     * Constructor for creating a new user (before database insertion).
     * Sets createdAt to current timestamp automatically.
     *
     * @param username the username for the new user
     * @param email the email address for the new user
     * @param password the hashed password for the new user
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Constructor for creating a user object from database records.
     * Used by DAO layer when retrieving existing users.
     *
     * @param userId the unique user identifier
     * @param username the username
     * @param email the email address
     * @param password the hashed password
     * @param createdAt the timestamp when the user was created
     */
    public User(int userId, String username, String email, String password, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns a string representation of the User object.
     * Password is excluded for security reasons.
     *
     * @return string representation without sensitive data
     */
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Checks equality based on userId (primary key).
     *
     * @param o object to compare
     * @return true if users have the same userId
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId;
    }

    /**
     * Generates hash code based on userId.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}