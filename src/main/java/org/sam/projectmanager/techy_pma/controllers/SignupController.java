package org.sam.projectmanager.techy_pma.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.sam.projectmanager.techy_pma.Main;
import org.sam.projectmanager.techy_pma.database.UserDAO;
import org.sam.projectmanager.techy_pma.models.User;
import org.sam.projectmanager.techy_pma.utils.PasswordUtil;
import org.sam.projectmanager.techy_pma.utils.Session;

import java.io.IOException;

/**
 * Controller for the signup screen
 */
public class SignupController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label passwordStrengthLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Button signupButton;

    @FXML
    private Hyperlink loginLink;

    /**
     * Initialize method (called automatically by JavaFX)
     */
    @FXML
    public void initialize() {
        // Add password strength indicator
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });

        // Add enter key listener to confirm password field
        confirmPasswordField.setOnAction(event -> handleSignup());
    }

    /**
     * Update password strength indicator
     */
    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            passwordStrengthLabel.setText("");
            passwordStrengthLabel.setVisible(false);
            return;
        }

        String strength = PasswordUtil.getPasswordStrength(password);
        passwordStrengthLabel.setText("Strength: " + strength);
        passwordStrengthLabel.setVisible(true);

        // Color coding
        switch (strength) {
            case "Weak":
                passwordStrengthLabel.setStyle("-fx-text-fill: #e74c3c;");
                break;
            case "Medium":
                passwordStrengthLabel.setStyle("-fx-text-fill: #f39c12;");
                break;
            case "Strong":
                passwordStrengthLabel.setStyle("-fx-text-fill: #27ae60;");
                break;
            default:
                passwordStrengthLabel.setStyle("-fx-text-fill: #95a5a6;");
        }
    }

    /**
     * Handle signup button click
     */
    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        // Validate username length
        if (username.length() < 3) {
            showError("Username must be at least 3 characters long");
            return;
        }

        // Validate email format (basic check)
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address");
            return;
        }

        // Validate password
        if (!PasswordUtil.isValidPassword(password)) {
            showError("Password must be at least 8 characters long");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Check if username already exists
        if (UserDAO.usernameExists(username)) {
            showError("Username already taken. Please choose another.");
            return;
        }

        // Check if email already exists
        if (UserDAO.emailExists(email)) {
            showError("Email already registered. Please use another or login.");
            return;
        }

        // Hash the password
        String hashedPassword = PasswordUtil.hashPassword(password);

        // Create user object
        User newUser = new User(username, email, hashedPassword);

        // Insert into database
        boolean success = UserDAO.insertUser(newUser);

        if (success) {
            Session.setCurrentUser(newUser);

            // Navigate to dashboard
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(
                        Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/dashboard.fxml")
                );
                Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
                Stage stage = (Stage) signupButton.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("TECHY | Project Manager - Dashboard");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error loading dashboard");
            }

        } else {
            showError("Signup failed. Please try again.");
        }
    }

    /**
     * Handle login link click (go back to login screen)
     */

    @FXML
    private void handleLoginLink() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 500);

            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Project Manager - Login");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading login screen");
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);
    }

    /**
     * Show success message
     */
    private void showSuccess(String message) {
        messageLabel.setText("✓ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setVisible(true);
    }

    /**
     * Hide message
     */
    private void hideMessage() {
        messageLabel.setVisible(false);
    }
}