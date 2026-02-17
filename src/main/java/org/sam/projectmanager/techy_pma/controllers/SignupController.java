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
 * Manages the user registration flow, including input validation,
 * visual feedback, and database persistence.
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
     * Sets up real-time listeners for input validation and UI feedback.
     * Called automatically by the FXML loader.
     */
    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });

        confirmPasswordField.setOnAction(event -> handleSignup());
    }

    /**
     * Evaluates the password complexity and updates the UI label
     * with appropriate text and color coding (Red/Yellow/Green).
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
     * Validates inputs, persists the new user, and transitions to the Dashboard.
     */
    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters long");
            return;
        }

        // Basic format check; comprehensive validation relies on email confirmation
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address");
            return;
        }

        if (!PasswordUtil.isValidPassword(password)) {
            showError("Password must be at least 8 characters long");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (UserDAO.usernameExists(username)) {
            showError("Username already taken. Please choose another.");
            return;
        }

        if (UserDAO.emailExists(email)) {
            showError("Email already registered. Please use another or login.");
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        User newUser = new User(username, email, hashedPassword);

        boolean success = UserDAO.insertUser(newUser);

        if (success) {
            Session.setCurrentUser(newUser);

            // Context switch: Load Dashboard scene
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
     * Navigates back to the Login screen.
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
     * Updates the status label with error styling (Red).
     */
    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);
    }

    /**
     * Updates the status label with success styling (Green).
     */
    private void showSuccess(String message) {
        messageLabel.setText("✓ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setVisible(true);
    }

    private void hideMessage() {
        messageLabel.setVisible(false);
    }
}