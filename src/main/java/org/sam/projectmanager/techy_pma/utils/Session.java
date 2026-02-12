package org.sam.projectmanager.techy_pma.utils;

import org.sam.projectmanager.techy_pma.models.User;

/**
 * Session management class to track the currently logged-in user.
 * This is a singleton pattern - only one user can be logged in at a time.
 */
public class Session {

    // Static field to hold the current user (shared across entire application)
    private static User currentUser = null;

    /**
     * Set the current logged-in user
     * @param user The user who just logged in
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            System.out.println("✓ Session started for user: " + user.getUsername());
        }
    }

    /**
     * Get the current logged-in user
     * @return The current user, or null if no one is logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in
     * @return true if someone is logged in, false otherwise
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clear the session (log out the current user)
     */
    public static void clearSession() {
        if (currentUser != null) {
            System.out.println("✓ Session ended for user: " + currentUser.getUsername());
        }
        currentUser = null;
    }

    /**
     * Get the current user's ID
     * @return The user ID, or -1 if no one is logged in
     */
    public static int getCurrentUserId() {
        if (currentUser != null) {
            return currentUser.getUserId();
        }
        return -1;
    }

    /**
     * Get the current user's username
     * @return The username, or null if no one is logged in
     */
    public static String getCurrentUsername() {
        if (currentUser != null) {
            return currentUser.getUsername();
        }
        return null;
    }

    /**
     * Get the current user's email
     * @return The email, or null if no one is logged in
     */
    public static String getCurrentUserEmail() {
        if (currentUser != null) {
            return currentUser.getEmail();
        }
        return null;
    }

    /**
     * Update the current user's information in the session
     * (Call this after updating user info in database)
     * @param updatedUser The updated user object
     */
    public static void updateCurrentUser(User updatedUser) {
        if (currentUser != null && updatedUser != null) {
            if (currentUser.getUserId() == updatedUser.getUserId()) {
                currentUser = updatedUser;
                System.out.println("✓ Session updated for user: " + updatedUser.getUsername());
            }
        }
    }

    /**
     * Check if the current user is a specific user
     * @param userId The user ID to check
     * @return true if current user matches, false otherwise
     */
    public static boolean isCurrentUser(int userId) {
        return currentUser != null && currentUser.getUserId() == userId;
    }
}
