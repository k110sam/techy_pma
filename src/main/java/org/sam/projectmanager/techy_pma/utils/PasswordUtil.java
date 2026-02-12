package org.sam.projectmanager.techy_pma.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 * This class ensures passwords are never stored in plain text.
 */
public class PasswordUtil {

    /**
     * Hash a plain text password using BCrypt
     * @param plainPassword The plain text password to hash
     * @return The hashed password (safe to store in database)
     */
    public static String hashPassword(String plainPassword) {
        // BCrypt.gensalt() generates a random salt
        // BCrypt.hashpw() creates the hash
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verify a plain text password against a hashed password
     * @param plainPassword The plain text password entered by user
     * @param hashedPassword The hashed password from database
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            // BCrypt.checkpw() compares plain text with hash
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid hash format
            System.err.println("Error verifying password: Invalid hash format");
            return false;
        }
    }

    /**
     * Check if a password meets minimum security requirements
     * @param password The password to validate
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // You can add more rules here:

        return true;
    }

    /**
     * Get password strength description
     * @param password The password to check
     * @return A string describing password strength
     */
    public static String getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "Empty";
        }

        if (password.length() < 6) {
            return "Weak";
        }

        if (password.length() < 10) {
            return "Medium";
        }

        // Check for variety of characters
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        int variety = 0;
        if (hasUpper) variety++;
        if (hasLower) variety++;
        if (hasDigit) variety++;
        if (hasSpecial) variety++;

        if (password.length() >= 12 && variety >= 3) {
            return "Strong";
        }

        return "Medium";
    }
}