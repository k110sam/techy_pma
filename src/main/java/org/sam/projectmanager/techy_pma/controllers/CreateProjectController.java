package org.sam.projectmanager.techy_pma.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.sam.projectmanager.techy_pma.Main;
import org.sam.projectmanager.techy_pma.database.ProjectDAO;
import org.sam.projectmanager.techy_pma.database.ProjectMemberDAO;
import org.sam.projectmanager.techy_pma.models.Project;
import org.sam.projectmanager.techy_pma.models.ProjectMember;
import org.sam.projectmanager.techy_pma.utils.Session;

import java.io.IOException;

/**
 * Controller for the Create Project screen.
 *
 * <p>Handles form input, validation, project creation, and automatically
 * assigns the logged-in user as the project Owner upon successful creation.</p>
 *
 * Bound to: {@code create-project.fxml}
 */
public class CreateProjectController {

    // ─── SIDEBAR ───
    @FXML private Label avatarLabel;       // First letter of username shown in avatar circle
    @FXML private Label sidebarUsername;   // Logged-in user's username
    @FXML private Label sidebarEmail;      // Logged-in user's email

    // ─── FORM FIELDS ───
    @FXML private TextField projectNameField;         // Required — minimum 3 characters
    @FXML private TextArea projectDescriptionField;   // Optional free-text description
    @FXML private ComboBox<String> statusComboBox;    // Initial status selection
    @FXML private Slider progressSlider;              // Sets initial progress (0–100)
    @FXML private Label progressValueLabel;           // Live display of slider value e.g. "45%"

    // ─── FEEDBACK ───
    @FXML private Label messageLabel;    // Displays success or error messages below the form
    @FXML private Button createButton;  // Disabled after successful creation to prevent duplicates

    // ─────────────────────────────────────────────────────────────────────────
    // INITIALIZATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called automatically by JavaFX once all @FXML fields are injected.
     * Sets up the sidebar, status dropdown, and progress slider.
     */
    @FXML
    public void initialize() {
        loadUserInfo();
        setupStatusComboBox();
        setupProgressSlider();
    }

    /**
     * Populates sidebar labels with the current user's username and email.
     * Uses the first character of the username as the avatar letter.
     */
    private void loadUserInfo() {
        if (Session.getCurrentUser() == null) return;
        sidebarUsername.setText(Session.getCurrentUser().getUsername());
        sidebarEmail.setText(Session.getCurrentUser().getEmail());
        avatarLabel.setText(
                String.valueOf(Session.getCurrentUser().getUsername().charAt(0)).toUpperCase()
        );
    }

    /**
     * Populates the status dropdown with all valid project statuses.
     * Defaults to "not started" for new projects.
     */
    private void setupStatusComboBox() {
        statusComboBox.getItems().addAll(
                "not started",
                "in progress",
                "completed",
                "published"
        );
        statusComboBox.setValue("not started");
    }

    /**
     * Attaches a listener to the progress slider so the percentage label
     * updates in real time as the user drags the slider.
     */
    private void setupProgressSlider() {
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int progress = newVal.intValue();
            progressValueLabel.setText(progress + "%");
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE PROJECT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles the Create Project button click.
     *
     * <p>Validates all form inputs, creates the project in the database,
     * then automatically adds the current user as Owner in project_members.
     * Navigates back to the Dashboard on success.</p>
     */
    @FXML
    private void handleCreateProject() {
        String name = projectNameField.getText().trim();
        String description = projectDescriptionField.getText().trim();
        String status = statusComboBox.getValue();
        int progress = (int) progressSlider.getValue();

        // ─── VALIDATION ───
        if (name.isEmpty()) {
            showError("Project name is required");
            return;
        }

        if (name.length() < 3) {
            showError("Project name must be at least 3 characters");
            return;
        }

        if (status == null || status.isEmpty()) {
            showError("Please select a status");
            return;
        }

        int userId = Session.getCurrentUserId();

        // Build the project object with form values
        Project newProject = new Project(name, description, userId);
        newProject.setStatus(status);
        newProject.setProjectProgress(progress);

        // Persist to database and retrieve the generated project ID
        int projectId = ProjectDAO.insertProject(newProject);

        if (projectId > 0) {
            // Creator is automatically assigned the Owner role
            ProjectMember ownerMembership = new ProjectMember(projectId, userId, "Owner");
            ProjectMemberDAO.addMember(ownerMembership);

            showSuccess("Project '" + name + "' created successfully!");
            createButton.setDisable(true); // Prevent duplicate submissions
            System.out.println("✓ Project created with ID: " + projectId);

            navigateToDashboard();
        } else {
            showError("Failed to create project. Please try again.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────────────────

    /** Navigates to the Dashboard screen */
    @FXML
    private void handleNavDashboard() {
        navigateToDashboard();
    }

    /** Navigates to the Browse Projects screen (used for My Projects sidebar button) */
    @FXML
    private void handleNavMyProjects() {
        navigateTo("browse-projects.fxml", "My Projects", 1100, 700);
    }

    /** Navigates to the Browse Projects screen */
    @FXML
    private void handleNavBrowse() {
        navigateTo("browse-projects.fxml", "Browse Projects", 1100, 700);
    }

    /** Clears the session and navigates back to the Login screen */
    @FXML
    private void handleLogout() {
        Session.clearSession();
        navigateTo("login.fxml", "Login", 600, 500);
    }

    /** Convenience wrapper that navigates to the Dashboard at its standard size */
    private void navigateToDashboard() {
        navigateTo("dashboard.fxml", "Dashboard", 1100, 700);
    }

    /**
     * Loads a new FXML screen and replaces the current scene on the active Stage.
     *
     * @param fxmlFile  Filename only e.g. {@code "dashboard.fxml"} — path is prepended automatically
     * @param title     Window title shown in the title bar
     * @param width     Scene width in pixels
     * @param height    Scene height in pixels
     */
    private void navigateTo(String fxmlFile, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/" + fxmlFile)
            );
            Scene scene = new Scene(loader.load(), width, height);
            Stage stage = (Stage) projectNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TECHY | Project Manager - " + title);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FEEDBACK HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Shows a red error message in the feedback label */
    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);
    }

    /** Shows a green success message in the feedback label */
    private void showSuccess(String message) {
        messageLabel.setText("✓ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setVisible(true);
    }
}