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

public class CreateProjectController {

    @FXML private Label avatarLabel;
    @FXML private Label sidebarUsername;
    @FXML private Label sidebarEmail;
    @FXML private TextField projectNameField;
    @FXML private TextArea projectDescriptionField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Slider progressSlider;
    @FXML private Label progressValueLabel;
    @FXML private Label messageLabel;
    @FXML private Button createButton;

    @FXML
    public void initialize() {
        loadUserInfo();
        setupStatusComboBox();
        setupProgressSlider();
    }

    /**
     * Load current user info into sidebar
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
     * Setup status dropdown options
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
     * Setup progress slider listener
     */
    private void setupProgressSlider() {
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int progress = newVal.intValue();
            progressValueLabel.setText(progress + "%");
        });
    }

    /**
     * Handle create project button
     */
    @FXML
    private void handleCreateProject() {
        String name = projectNameField.getText().trim();
        String description = projectDescriptionField.getText().trim();
        String status = statusComboBox.getValue();
        int progress = (int) progressSlider.getValue();

        // Validation
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

        // Create project object
        Project newProject = new Project(name, description, userId);
        newProject.setStatus(status);
        newProject.setProjectProgress(progress);

        // Save to database
        int projectId = ProjectDAO.insertProject(newProject);

        if (projectId > 0) {
            // Add creator as Owner in project_members
            ProjectMember ownerMembership = new ProjectMember(projectId, userId, "Owner");
            ProjectMemberDAO.addMember(ownerMembership);

            showSuccess("Project '" + name + "' created successfully!");
            createButton.setDisable(true);

            System.out.println("✓ Project created with ID: " + projectId);

            // Navigate to dashboard after short delay
            navigateToDashboard();

        } else {
            showError("Failed to create project. Please try again.");
        }
    }

    // ─── NAVIGATION ───

    @FXML
    private void handleNavDashboard() {
        navigateToDashboard();
    }

    @FXML
    private void handleNavMyProjects() {
        navigateTo("browse-projects.fxml", "My Projects", 1100, 700);
    }

    @FXML
    private void handleNavBrowse() {
        navigateTo("browse-projects.fxml", "Browse Projects", 1100, 700);
    }

    @FXML
    private void handleLogout() {
        Session.clearSession();
        navigateTo("login.fxml", "Login", 600, 500);
    }

    private void navigateToDashboard() {
        navigateTo("dashboard.fxml", "Dashboard", 1100, 700);
    }

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

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        messageLabel.setText("✓ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setVisible(true);
    }
}