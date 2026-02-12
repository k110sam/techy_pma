package org.sam.projectmanager.techy_pma.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.sam.projectmanager.techy_pma.Main;
import org.sam.projectmanager.techy_pma.database.ProjectDAO;
import org.sam.projectmanager.techy_pma.database.ProjectMemberDAO;
import org.sam.projectmanager.techy_pma.database.UserDAO;
import org.sam.projectmanager.techy_pma.models.Project;
import org.sam.projectmanager.techy_pma.models.ProjectMember;
import org.sam.projectmanager.techy_pma.models.User;
import org.sam.projectmanager.techy_pma.utils.SelectedProject;
import org.sam.projectmanager.techy_pma.utils.Session;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProjectDetailsController {

    // ─── SIDEBAR ───
    @FXML private Label avatarLabel;
    @FXML private Label sidebarUsername;
    @FXML private Label sidebarEmail;

    // ─── TOP BAR ───
    @FXML private Label projectTitleLabel;
    @FXML private Label projectSubLabel;

    // ─── PROJECT INFO ───
    @FXML private Label projectNameLabel;
    @FXML private Label ownerLabel;
    @FXML private Label statusBadgeLabel;
    @FXML private Label roleBadgeLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label progressPercentLabel;
    @FXML private ProgressBar detailProgressBar;

    // ─── CONTROLS ───
    @FXML private VBox ownerControls;
    @FXML private HBox memberControls;
    @FXML private Slider updateProgressSlider;
    @FXML private Label updateProgressLabel;
    @FXML private ComboBox<String> updateStatusComboBox;

    // ─── MEMBERS ───
    @FXML private Label memberCountLabel;
    @FXML private VBox membersContainer;

    private Project currentProject;

    @FXML
    public void initialize() {
        loadUserInfo();
        loadProjectDetails();
        setupUpdateControls();
    }

    private void loadUserInfo() {
        if (Session.getCurrentUser() == null) return;
        sidebarUsername.setText(Session.getCurrentUser().getUsername());
        sidebarEmail.setText(Session.getCurrentUser().getEmail());
        avatarLabel.setText(
                String.valueOf(Session.getCurrentUser().getUsername().charAt(0)).toUpperCase()
        );
    }

    private void loadProjectDetails() {
        currentProject = SelectedProject.getProject();
        if (currentProject == null) {
            projectTitleLabel.setText("No project selected");
            return;
        }

        int userId = Session.getCurrentUserId();
        int projectId = currentProject.getProjectId();

        // Top bar
        projectTitleLabel.setText(currentProject.getProjectName());
        projectSubLabel.setText("Project ID: " + projectId);

        // Project name and owner
        projectNameLabel.setText(currentProject.getProjectName());
        User owner = UserDAO.getUserById(currentProject.getCreatedBy());
        ownerLabel.setText("⊙  Created by: " + (owner != null ? owner.getUsername() : "Unknown"));

        // Status badge
        statusBadgeLabel.setText(currentProject.getStatus().toUpperCase());
        statusBadgeLabel.getStyleClass().setAll("badge", getStatusBadgeClass(currentProject.getStatus()));

        // Role badge
        String role = ProjectMemberDAO.getUserRole(projectId, userId);
        roleBadgeLabel.setText(role != null ? role.toUpperCase() : "MEMBER");

        // Description
        String desc = currentProject.getProjectDescription();
        descriptionLabel.setText((desc != null && !desc.isEmpty()) ? desc : "No description provided.");

        // Created at
        if (currentProject.getCreatedAt() != null) {
            createdAtLabel.setText(
                    currentProject.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            );
        }

        // Progress
        int progress = currentProject.getProjectProgress();
        progressPercentLabel.setText(progress + "%");
        detailProgressBar.setProgress(progress / 100.0);

        // Show controls based on role
        if ("Owner".equals(role) || "Admin".equals(role)) {
            ownerControls.setVisible(true);
            ownerControls.setManaged(true);
            updateProgressSlider.setValue(progress);
        } else if ("Member".equals(role)) {
            memberControls.setVisible(true);
            memberControls.setManaged(true);
        }

        // Load members
        loadMembers(projectId);
    }

    private void setupUpdateControls() {
        // Progress slider listener
        updateProgressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateProgressLabel.setText(newVal.intValue() + "%");
        });

        // Status combo
        updateStatusComboBox.getItems().addAll(
                "not started", "in progress", "completed", "published"
        );
        if (currentProject != null) {
            updateStatusComboBox.setValue(currentProject.getStatus());
        }
    }

    private void loadMembers(int projectId) {
        List<ProjectMember> members = ProjectMemberDAO.getMembersByProject(projectId);
        membersContainer.getChildren().clear();
        memberCountLabel.setText(members.size() + " member(s)");

        for (ProjectMember member : members) {
            User user = UserDAO.getUserById(member.getUserId());
            if (user != null) {
                HBox memberRow = createMemberRow(user, member.getRole());
                membersContainer.getChildren().add(memberRow);
            }
        }
    }

    private HBox createMemberRow(User user, String role) {
        HBox row = new HBox(14);
        row.getStyleClass().add("member-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Avatar
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("member-avatar");
        Label avatarLetter = new Label(
                String.valueOf(user.getUsername().charAt(0)).toUpperCase()
        );
        avatarLetter.getStyleClass().add("member-avatar-letter");
        avatar.getChildren().add(avatarLetter);

        // User info
        VBox userInfo = new VBox(3);
        HBox.setHgrow(userInfo, Priority.ALWAYS);

        Label nameLabel = new Label(user.getUsername());
        nameLabel.getStyleClass().add("member-name");

        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().add("member-email");

        userInfo.getChildren().addAll(nameLabel, emailLabel);

        // Role badge
        Label roleBadge = new Label(role.toUpperCase());
        roleBadge.getStyleClass().add("role-badge");

        // Joined at indicator for current user
        if (user.getUserId() == Session.getCurrentUserId()) {
            Label youLabel = new Label("(You)");
            youLabel.setStyle("-fx-text-fill: #4fc3f7; -fx-font-size: 11px;");
            row.getChildren().addAll(avatar, userInfo, youLabel, roleBadge);
        } else {
            row.getChildren().addAll(avatar, userInfo, roleBadge);
        }

        return row;
    }

    @FXML
    private void handleUpdateProgress() {
        if (currentProject == null) return;

        int newProgress = (int) updateProgressSlider.getValue();
        boolean updated = ProjectDAO.updateProgress(currentProject.getProjectId(), newProgress);

        if (updated) {
            currentProject.setProjectProgress(newProgress);
            progressPercentLabel.setText(newProgress + "%");
            detailProgressBar.setProgress(newProgress / 100.0);
            showAlert("Success", "Progress updated to " + newProgress + "%");
        } else {
            showAlert("Error", "Failed to update progress.");
        }
    }

    @FXML
    private void handleUpdateStatus() {
        if (currentProject == null) return;

        String newStatus = updateStatusComboBox.getValue();
        if (newStatus == null) return;

        boolean updated = ProjectDAO.updateStatus(currentProject.getProjectId(), newStatus);

        if (updated) {
            currentProject.setStatus(newStatus);
            statusBadgeLabel.setText(newStatus.toUpperCase());
            statusBadgeLabel.getStyleClass().setAll("badge", getStatusBadgeClass(newStatus));
            showAlert("Success", "Status updated to '" + newStatus + "'");
        } else {
            showAlert("Error", "Failed to update status.");
        }
    }

    @FXML
    private void handleLeaveProject() {
        if (currentProject == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Leave Project");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to leave '" + currentProject.getProjectName() + "'?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean removed = ProjectMemberDAO.removeMember(
                        currentProject.getProjectId(),
                        Session.getCurrentUserId()
                );

                if (removed) {
                    showAlert("Left Project", "You have left the project.");
                    SelectedProject.clear();
                    navigateTo("dashboard.fxml", "Dashboard", 1100, 700);
                } else {
                    showAlert("Error", "Failed to leave project.");
                }
            }
        });
    }

    private String getStatusBadgeClass(String status) {
        return switch (status.toLowerCase()) {
            case "not started" -> "badge-not-started";
            case "in progress" -> "badge-in-progress";
            case "completed" -> "badge-completed";
            case "published" -> "badge-published";
            default -> "badge-not-started";
        };
    }

    // ─── NAVIGATION ───

    @FXML private void handleNavDashboard() { navigateTo("dashboard.fxml", "Dashboard", 1100, 700); }
    @FXML private void handleNavMyProjects() { navigateTo("browse-projects.fxml", "Browse", 1100, 700); }
    @FXML private void handleNavBrowse() { navigateTo("browse-projects.fxml", "Browse Projects", 1100, 700); }
    @FXML private void handleCreateProject() { navigateTo("create-project.fxml", "Create Project", 1100, 700); }

    @FXML
    private void handleLogout() {
        Session.clearSession();
        SelectedProject.clear();
        navigateTo("login.fxml", "Login", 600, 500);
    }

    private void navigateTo(String fxmlFile, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/" + fxmlFile)
            );
            Scene scene = new Scene(loader.load(), width, height);
            Stage stage = (Stage) projectNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TECHY | Project Manager - " + title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}