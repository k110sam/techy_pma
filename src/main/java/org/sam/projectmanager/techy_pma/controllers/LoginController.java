package org.sam.projectmanager.techy_pma.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.sam.projectmanager.techy_pma.database.UserDAO;
import org.sam.projectmanager.techy_pma.models.User;
import org.sam.projectmanager.techy_pma.utils.PasswordUtil;
import org.sam.projectmanager.techy_pma.utils.Session;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.sam.projectmanager.techy_pma.Main;
import java.io.IOException;

/**
 * Controller for the login screen
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signupLink;

    /**
     * Initialize method (called automatically by JavaFX)
     */
    @FXML
    public void initialize() {
        // Add enter key listener to password field
        passwordField.setOnAction(event -> handleLogin());
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Get user from database
        User user = UserDAO.getUserByUsername(username);

        // Check if user exists
        if (user == null) {
            showError("User not found");
            return;
        }

        // Verify password
        if (PasswordUtil.verifyPassword(password, user.getPassword())) {
            // Login successful!
            Session.setCurrentUser(user);

            // Hide error
            hideError();

            // Navigate to dashboard
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(
                        Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/dashboard.fxml")
                );
                Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("TECHY | Project Manager - Dashboard");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error loading dashboard");
            }
        } else {
            // Wrong password
            showError("Incorrect password");
        }
    }

    /**
     * Handle signup link click
     */
    @FXML
    private void handleSignupLink() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/signup.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 600);

            Stage stage = (Stage) signupLink.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TECHY Project Manager - Sign Up");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading signup screen");
        }
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setVisible(true);
    }

    /**
     * Show success message
     */
    private void showSuccess(String message) {
        errorLabel.setText("✓ " + message);
        errorLabel.setStyle("-fx-text-fill: #27ae60;");
        errorLabel.setVisible(true);
    }

    /**
     * Show info message
     */
    private void showInfo(String message) {
        errorLabel.setText("ℹ " + message);
        errorLabel.setStyle("-fx-text-fill: #3498db;");
        errorLabel.setVisible(true);
    }

    /**
     * Hide error message
     */
    private void hideError() {
        errorLabel.setVisible(false);
    }
}