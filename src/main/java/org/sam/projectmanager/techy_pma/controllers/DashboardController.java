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
import org.sam.projectmanager.techy_pma.models.User;
import org.sam.projectmanager.techy_pma.utils.Session;
import org.sam.projectmanager.techy_pma.utils.SelectedProject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Dashboard screen
 */
public class DashboardController {

    // ─── TOP BAR ───
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;

    // ─── SIDEBAR ───
    @FXML private Label avatarLabel;
    @FXML private Label sidebarUsername;
    @FXML private Label sidebarEmail;

    // ─── STAT CARDS ───
    @FXML private Label myProjectsCount;
    @FXML private Label createdCount;
    @FXML private Label availableCount;

    // ─── PROJECTS ───
    @FXML private Label projectCountLabel;
    @FXML private VBox projectsContainer;
    @FXML private VBox emptyState;

    /**
     * Called automatically by JavaFX when the FXML is loaded
     */
    @FXML
    public void initialize() {
        loadUserInfo();
        loadStats();
        loadMyProjects();
        setDate();
    }

    /**
     * Load current user info into sidebar and top bar
     */
    private void loadUserInfo() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        // Top bar greeting
        String hour = String.valueOf(LocalDateTime.now().getHour());
        String greeting = getGreeting();
        welcomeLabel.setText(greeting + ", " + currentUser.getUsername() + "!");

        // Sidebar
        sidebarUsername.setText(currentUser.getUsername());
        sidebarEmail.setText(currentUser.getEmail());

        // Avatar letter (first letter of username)
        avatarLabel.setText(
                String.valueOf(currentUser.getUsername().charAt(0)).toUpperCase()
        );
    }

    /**
     * Get greeting based on time of day
     */
    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    /**
     * Set the current date in the top bar
     */
    private void setDate() {
        String date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy")
        );
        dateLabel.setText(date);
    }

    /**
     * Load stat card numbers
     */
    private void loadStats() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        int userId = currentUser.getUserId();

        // My projects count (projects user is a member of)
        List<Project> myProjects = ProjectDAO.getProjectsByUser(userId);
        myProjectsCount.setText(String.valueOf(myProjects.size()));

        // Created count (projects user created)
        List<Project> createdProjects = ProjectDAO.getProjectsCreatedByUser(userId);
        createdCount.setText(String.valueOf(createdProjects.size()));

        // Available count (all projects)
        List<Project> allProjects = ProjectDAO.getAllProjects();
        availableCount.setText(String.valueOf(allProjects.size()));
    }

    /**
     * Load the current user's projects into the projects container
     */
    private void loadMyProjects() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        int userId = currentUser.getUserId();

        // Get projects user is a member of
        List<Project> myProjects = ProjectDAO.getProjectsByUser(userId);

        // Clear existing cards
        projectsContainer.getChildren().clear();

        if (myProjects.isEmpty()) {
            // Show empty state
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            projectCountLabel.setText("0 projects");
        } else {
            // Hide empty state
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            projectCountLabel.setText(myProjects.size() + " project(s)");

            // Create a card for each project
            for (Project project : myProjects) {
                VBox card = createProjectCard(project);
                projectsContainer.getChildren().add(card);
            }
        }
    }

    /**
     * Create a project card UI element dynamically
     */
    private VBox createProjectCard(Project project) {
        // ─── CARD CONTAINER ───
        VBox card = new VBox(12);
        card.getStyleClass().add("project-card");

        // ─── TOP ROW: Name + Badges ───
        HBox topRow = new HBox(10);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(project.getProjectName());
        nameLabel.getStyleClass().add("project-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Status badge
        Label statusBadge = new Label(project.getStatus().toUpperCase());
        statusBadge.getStyleClass().addAll("badge", getStatusBadgeClass(project.getStatus()));

        // Role badge
        String role = ProjectMemberDAO.getUserRole(
                project.getProjectId(),
                Session.getCurrentUserId()
        );
        Label roleBadge = new Label(role != null ? role.toUpperCase() : "MEMBER");
        roleBadge.getStyleClass().add("role-badge");

        topRow.getChildren().addAll(nameLabel, statusBadge, roleBadge);

        // ─── OWNER ROW ───
        User owner = UserDAO.getUserById(project.getCreatedBy());
        String ownerName = owner != null ? owner.getUsername() : "Unknown";
        Label ownerLabel = new Label("⊙  Owner: " + ownerName);
        ownerLabel.getStyleClass().add("project-owner");

        // ─── DESCRIPTION ───
        String desc = project.getProjectDescription();
        if (desc == null || desc.isEmpty()) desc = "No description provided.";
        if (desc.length() > 120) desc = desc.substring(0, 120) + "...";
        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("project-description");
        descLabel.setWrapText(true);

        // ─── PROGRESS ROW ───
        HBox progressRow = new HBox(10);
        progressRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ProgressBar progressBar = new ProgressBar(project.getProjectProgress() / 100.0);
        progressBar.getStyleClass().add("project-progress-bar");
        progressBar.setPrefHeight(6);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label progressText = new Label(project.getProjectProgress() + "%");
        progressText.getStyleClass().add("project-progress-text");

        progressRow.getChildren().addAll(progressBar, progressText);

        // ─── BOTTOM ROW: View Button ───
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button viewBtn = new Button("View Details →");
        viewBtn.getStyleClass().add("project-view-btn");
        viewBtn.setOnAction(e -> handleViewProject(project));

        bottomRow.getChildren().add(viewBtn);

        // ─── ASSEMBLE CARD ───
        card.getChildren().addAll(topRow, ownerLabel, descLabel, progressRow, bottomRow);

        return card;
    }

    /**
     * Get the CSS class for a status badge
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

    /**
     * Handle viewing a project's details
     */
    private void handleViewProject(Project project) {
        SelectedProject.setProject(project);
        navigateTo("project-details.fxml", "Project Details", 1100, 700);
    }

    // ─── NAVIGATION HANDLERS ───


    @FXML
    private void handleNavDashboard() {
        // Already on dashboard - refresh
        loadMyProjects();
        loadStats();
    }

    @FXML
    private void handleNavBrowse() {
        navigateTo("browse-projects.fxml", "Browse Projects", 1100, 700);
    }

    @FXML
    private void handleCreateProject() {
        navigateTo("create-project.fxml", "Create Project", 1100, 700);
    }

    @FXML
    private void handleNavMyProjects() {
        navigateTo("browse-projects.fxml", "My Projects", 1100, 700);
    }


    private void navigateTo(String fxmlFile, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/" + fxmlFile)
            );
            Scene scene = new Scene(loader.load(), width, height);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TECHY | Project Manager - " + title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // Clear session
        Session.clearSession();

        // Navigate back to login
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/login.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load(), 600, 500);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TECHY | Project Manager - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper to show an info alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}