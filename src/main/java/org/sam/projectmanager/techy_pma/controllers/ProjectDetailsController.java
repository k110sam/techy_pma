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

/**
 * Controller responsible for managing the Project Details screen.
 *
 * <p>This controller:
 * <ul>
 *     <li>Displays detailed information about a selected project</li>
 *     <li>Loads project members dynamically</li>
 *     <li>Controls role-based UI visibility (Owner/Admin/Member)</li>
 *     <li>Handles project updates (progress and status)</li>
 *     <li>Handles leaving a project</li>
 *     <li>Manages navigation between application screens</li>
 * </ul>
 *
 * <p>Dependencies:
 * <ul>
 *     <li>{@link ProjectDAO} for project updates</li>
 *     <li>{@link ProjectMemberDAO} for membership operations</li>
 *     <li>{@link UserDAO} for retrieving user information</li>
 *     <li>{@link Session} for current logged-in user</li>
 *     <li>{@link SelectedProject} for active project state</li>
 * </ul>
 *
 * <p>Follows the MVC pattern where:
 * <ul>
 *     <li>The View is defined in project-details.fxml</li>
 *     <li>This class acts as the Controller</li>
 *     <li>Project and User models represent application data</li>
 * </ul>
 */
public class ProjectDetailsController {

    // ───────────────── SIDEBAR COMPONENTS ─────────────────

    /** Displays first letter of logged-in user's username */
    @FXML private Label avatarLabel;

    /** Displays logged-in user's username */
    @FXML private Label sidebarUsername;

    /** Displays logged-in user's email */
    @FXML private Label sidebarEmail;

    // ───────────────── TOP BAR COMPONENTS ─────────────────

    /** Displays project title at top of page */
    @FXML private Label projectTitleLabel;

    /** Displays project ID beneath the title */
    @FXML private Label projectSubLabel;

    // ───────────────── PROJECT INFORMATION ─────────────────

    /** Displays project name */
    @FXML private Label projectNameLabel;

    /** Displays project creator information */
    @FXML private Label ownerLabel;

    /** Displays project status badge */
    @FXML private Label statusBadgeLabel;

    /** Displays current user's role badge */
    @FXML private Label roleBadgeLabel;

    /** Displays project description */
    @FXML private Label descriptionLabel;

    /** Displays project creation date */
    @FXML private Label createdAtLabel;

    /** Displays project progress percentage text */
    @FXML private Label progressPercentLabel;

    /** Visual progress bar representing project completion */
    @FXML private ProgressBar detailProgressBar;

    // ───────────────── ROLE-BASED CONTROLS ─────────────────

    /** Controls visible only to Owners/Admins */
    @FXML private VBox ownerControls;

    /** Controls visible only to Members */
    @FXML private HBox memberControls;

    /** Slider used to update project progress */
    @FXML private Slider updateProgressSlider;

    /** Displays selected progress percentage */
    @FXML private Label updateProgressLabel;

    /** Dropdown used to update project status */
    @FXML private ComboBox<String> updateStatusComboBox;

    // ───────────────── MEMBERS SECTION ─────────────────

    /** Displays number of project members */
    @FXML private Label memberCountLabel;

    /** Container that dynamically holds member rows */
    @FXML private VBox membersContainer;

    /** Holds currently selected project */
    private Project currentProject;

    /**
     * Initializes the controller.
     *
     * <p>This method is automatically called by JavaFX
     * after FXML fields are injected.
     *
     * <p>It loads:
     * <ul>
     *     <li>Logged-in user information</li>
     *     <li>Selected project details</li>
     *     <li>Update controls configuration</li>
     * </ul>
     */
    @FXML
    public void initialize() {
        loadUserInfo();
        loadProjectDetails();
        setupUpdateControls();
    }

    /**
     * Loads logged-in user information into sidebar.
     *
     * <p>Displays:
     * <ul>
     *     <li>Username</li>
     *     <li>Email</li>
     *     <li>Avatar initial</li>
     * </ul>
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
     * Loads selected project details and updates UI components.
     *
     * <p>Includes:
     * <ul>
     *     <li>Project metadata</li>
     *     <li>Status and role badges</li>
     *     <li>Description</li>
     *     <li>Progress information</li>
     *     <li>Role-based control visibility</li>
     *     <li>Project members list</li>
     * </ul>
     */
    private void loadProjectDetails() {

        currentProject = SelectedProject.getProject();

        if (currentProject == null) {
            projectTitleLabel.setText("No project selected");
            return;
        }

        int userId = Session.getCurrentUserId();
        int projectId = currentProject.getProjectId();

        projectTitleLabel.setText(currentProject.getProjectName());
        projectSubLabel.setText("Project ID: " + projectId);

        projectNameLabel.setText(currentProject.getProjectName());

        User owner = UserDAO.getUserById(currentProject.getCreatedBy());
        ownerLabel.setText("⊙  Created by: " + (owner != null ? owner.getUsername() : "Unknown"));

        statusBadgeLabel.setText(currentProject.getStatus().toUpperCase());
        statusBadgeLabel.getStyleClass().setAll("badge", getStatusBadgeClass(currentProject.getStatus()));

        String role = ProjectMemberDAO.getUserRole(projectId, userId);
        roleBadgeLabel.setText(role != null ? role.toUpperCase() : "MEMBER");

        String desc = currentProject.getProjectDescription();
        descriptionLabel.setText((desc != null && !desc.isEmpty()) ? desc : "No description provided.");

        if (currentProject.getCreatedAt() != null) {
            createdAtLabel.setText(
                    currentProject.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            );
        }

        int progress = currentProject.getProjectProgress();
        progressPercentLabel.setText(progress + "%");
        detailProgressBar.setProgress(progress / 100.0);

        if ("Owner".equals(role) || "Admin".equals(role)) {
            ownerControls.setVisible(true);
            ownerControls.setManaged(true);
            updateProgressSlider.setValue(progress);
        } else if ("Member".equals(role)) {
            memberControls.setVisible(true);
            memberControls.setManaged(true);
        }

        loadMembers(projectId);
    }

    /**
     * Configures update controls including:
     * <ul>
     *     <li>Progress slider listener</li>
     *     <li>Status dropdown options</li>
     * </ul>
     */
    private void setupUpdateControls() {

        updateProgressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateProgressLabel.setText(newVal.intValue() + "%");
        });

        updateStatusComboBox.getItems().addAll(
                "not started", "in progress", "completed", "published"
        );

        if (currentProject != null) {
            updateStatusComboBox.setValue(currentProject.getStatus());
        }
    }

    /**
     * Loads project members and dynamically generates UI rows.
     *
     * @param projectId ID of the project
     */
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

    /**
     * Creates a styled UI row representing a project member.
     *
     * @param user The member user
     * @param role The member role in the project
     * @return HBox representing the member row
     */
    private HBox createMemberRow(User user, String role) {

        HBox row = new HBox(14);
        row.getStyleClass().add("member-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("member-avatar");

        Label avatarLetter = new Label(
                String.valueOf(user.getUsername().charAt(0)).toUpperCase()
        );
        avatarLetter.getStyleClass().add("member-avatar-letter");

        avatar.getChildren().add(avatarLetter);

        VBox userInfo = new VBox(3);
        HBox.setHgrow(userInfo, Priority.ALWAYS);

        Label nameLabel = new Label(user.getUsername());
        nameLabel.getStyleClass().add("member-name");

        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().add("member-email");

        userInfo.getChildren().addAll(nameLabel, emailLabel);

        Label roleBadge = new Label(role.toUpperCase());
        roleBadge.getStyleClass().add("role-badge");

        if (user.getUserId() == Session.getCurrentUserId()) {
            Label youLabel = new Label("(You)");
            youLabel.setStyle("-fx-text-fill: #4fc3f7; -fx-font-size: 11px;");
            row.getChildren().addAll(avatar, userInfo, youLabel, roleBadge);
        } else {
            row.getChildren().addAll(avatar, userInfo, roleBadge);
        }

        return row;
    }

    /**
     * Handles updating project progress.
     *
     * Updates database and refreshes UI upon success.
     */
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

    /**
     * Handles updating project status.
     */
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

    /**
     * Handles leaving a project after confirmation.
     */
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

    /**
     * Returns CSS badge class based on project status.
     *
     * @param status project status
     * @return CSS class name
     */
    private String getStatusBadgeClass(String status) {
        return switch (status.toLowerCase()) {
            case "not started" -> "badge-not-started";
            case "in progress" -> "badge-in-progress";
            case "completed" -> "badge-completed";
            case "published" -> "badge-published";
            default -> "badge-not-started";
        };
    }

    // ───────────────── NAVIGATION METHODS ─────────────────

    @FXML private void handleNavDashboard() { navigateTo("dashboard.fxml", "Dashboard", 1100, 700); }
    @FXML private void handleNavMyProjects() { navigateTo("browse-projects.fxml", "Browse", 1100, 700); }
    @FXML private void handleNavBrowse() { navigateTo("browse-projects.fxml", "Browse Projects", 1100, 700); }
    @FXML private void handleCreateProject() { navigateTo("create-project.fxml", "Create Project", 1100, 700); }

    /**
     * Handles user logout.
     *
     * Clears session and selected project, then redirects to login screen.
     */
    @FXML
    private void handleLogout() {
        Session.clearSession();
        SelectedProject.clear();
        navigateTo("login.fxml", "Login", 600, 500);
    }

    /**
     * Loads a new FXML scene and replaces current stage scene.
     *
     * @param fxmlFile FXML file name
     * @param title Window title
     * @param width Scene width
     * @param height Scene height
     */
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

    /**
     * Displays an informational alert dialog.
     *
     * @param title Alert title
     * @param message Alert message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
