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
 * Controller responsible for managing the Login screen.
 *
 * <p>This controller handles:
 * <ul>
 *     <li>User input validation</li>
 *     <li>Authentication using {@link UserDAO}</li>
 *     <li>Password verification via {@link PasswordUtil}</li>
 *     <li>Session initialization using {@link Session}</li>
 *     <li>Navigation to Dashboard and Signup screens</li>
 * </ul>
 *
 * <p>It follows the MVC pattern where:
 * <ul>
 *     <li>The View is defined in the login FXML file</li>
 *     <li>This class acts as the Controller</li>
 *     <li>User data is represented by the {@link User} model</li>
 * </ul>
 */
public class LoginController {

    /** Text field for entering the username */
    @FXML
    private TextField usernameField;

    /** Password field for entering the user's password */
    @FXML
    private PasswordField passwordField;

    /** Label used to display error, success, or informational messages */
    @FXML
    private Label errorLabel;

    /** Button that triggers the login process */
    @FXML
    private Button loginButton;

    /** Hyperlink that redirects users to the signup screen */
    @FXML
    private Hyperlink signupLink;

    /**
     * Initializes the controller.
     *
     * <p>This method is automatically called by JavaFX after
     * the FXML components have been loaded and injected.
     *
     * <p>It sets up an event listener so that pressing the
     * "Enter" key inside the password field triggers login.
     */
    @FXML
    public void initialize() {
        passwordField.setOnAction(event -> handleLogin());
    }

    /**
     * Handles the login button click event.
     *
     * <p>Performs the following steps:
     * <ol>
     *     <li>Retrieves user input from text fields</li>
     *     <li>Validates that fields are not empty</li>
     *     <li>Fetches user record from database</li>
     *     <li>Verifies password against stored hash</li>
     *     <li>Creates session if authentication succeeds</li>
     *     <li>Loads the dashboard screen</li>
     * </ol>
     *
     * <p>If authentication fails, an appropriate error
     * message is displayed to the user.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate input fields
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Retrieve user from database
        User user = UserDAO.getUserByUsername(username);

        // Check if user exists
        if (user == null) {
            showError("User not found");
            return;
        }

        // Verify entered password against stored password hash
        if (PasswordUtil.verifyPassword(password, user.getPassword())) {

            // Store authenticated user in session
            Session.setCurrentUser(user);

            // Clear any previous error message
            hideError();

            // Navigate to dashboard screen
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
            // Password does not match
            showError("Incorrect password");
        }
    }

    /**
     * Handles click event on the signup hyperlink.
     *
     * <p>Loads the signup screen and replaces the current scene.
     * Displays an error message if loading fails.
     */
    @FXML
    private void handleSignupLink() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/signup.fxml")
            );
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
     * Displays an error message in the error label.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setVisible(true);
    }

    /**
     * Displays a success message in the error label.
     *
     * @param message The success message to display
     */
    private void showSuccess(String message) {
        errorLabel.setText("✓ " + message);
        errorLabel.setStyle("-fx-text-fill: #27ae60;");
        errorLabel.setVisible(true);
    }

    /**
     * Displays an informational message in the error label.
     *
     * @param message The informational message to display
     */
    private void showInfo(String message) {
        errorLabel.setText("ℹ " + message);
        errorLabel.setStyle("-fx-text-fill: #3498db;");
        errorLabel.setVisible(true);
    }

    /**
     * Hides the error label from view.
     */
    private void hideError() {
        errorLabel.setVisible(false);
    }
}
