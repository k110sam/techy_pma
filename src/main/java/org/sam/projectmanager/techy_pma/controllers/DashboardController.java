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
 * Controller for the Dashboard screen.
 *
 * <p>The main landing screen after login. Displays a personalised greeting,
 * project stat cards, quick action buttons, and a list of the user's current
 * projects as dynamically generated cards.</p>
 *
 * Bound to: {@code dashboard.fxml}
 */
public class DashboardController {

    // ─── TOP BAR ───
    @FXML private Label welcomeLabel;  // Personalised greeting e.g. "Good Morning, alice!"
    @FXML private Label dateLabel;     // Current date shown in top bar e.g. "Feb 17, 2026"

    // ─── SIDEBAR ───
    @FXML private Label avatarLabel;      // First letter of username shown in the avatar circle
    @FXML private Label sidebarUsername;  // Logged-in user's username
    @FXML private Label sidebarEmail;     // Logged-in user's email

    // ─── STAT CARDS ───
    @FXML private Label myProjectsCount;  // Count of projects the user is a member of
    @FXML private Label createdCount;     // Count of projects the user created
    @FXML private Label availableCount;   // Count of all projects in the system

    // ─── PROJECTS ───
    @FXML private Label projectCountLabel;  // Shows "X project(s)" above the project list
    @FXML private VBox projectsContainer;   // Dynamically populated with project cards
    @FXML private VBox emptyState;          // Shown when the user has no projects yet

    // ─────────────────────────────────────────────────────────────────────────
    // INITIALIZATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called automatically by JavaFX once all @FXML fields are injected.
     * Populates the sidebar, top bar greeting and date, stat cards, and project list.
     */
    @FXML
    public void initialize() {
        loadUserInfo();
        loadStats();
        loadMyProjects();
        setDate();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SETUP HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads the current user's info from the active {@link Session} and populates
     * the top bar greeting, sidebar username, email, and avatar letter.
     * Uses {@link #getGreeting()} to determine the time-appropriate greeting.
     */
    private void loadUserInfo() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        // Top bar greeting
        String hour = String.valueOf(LocalDateTime.now().getHour()); // reserved for future greeting customisation
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
     * Returns a time-appropriate greeting string based on the current hour.
     * Morning: before 12:00 | Afternoon: 12:00–16:59 | Evening: 17:00+
     */
    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    /**
     * Formats today's date and sets it in the top bar date label.
     * Format: {@code MMM dd, yyyy} e.g. "Feb 17, 2026"
     */
    private void setDate() {
        String date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy")
        );
        dateLabel.setText(date);
    }

    /**
     * Fetches project counts from the database and populates the three stat cards.
     * Each stat is fetched independently to keep counts accurate and separate.
     */
    private void loadStats() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        int userId = currentUser.getUserId();

        // My projects count (projects user is a member of)
        List<Project> myProjects = ProjectDAO.getProjectsByUser(userId);
        myProjectsCount.setText(String.valueOf(myProjects.size()));

        // Created count (projects user originally created)
        List<Project> createdProjects = ProjectDAO.getProjectsCreatedByUser(userId);
        createdCount.setText(String.valueOf(createdProjects.size()));

        // Available count (all projects in the system)
        List<Project> allProjects = ProjectDAO.getAllProjects();
        availableCount.setText(String.valueOf(allProjects.size()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PROJECT LIST
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fetches the current user's projects and renders them as cards in {@link #projectsContainer}.
     * Shows the {@link #emptyState} panel if the user has no projects yet.
     * Clears existing cards before re-rendering to prevent duplicates on refresh.
     */
    private void loadMyProjects() {
        User currentUser = Session.getCurrentUser();
        if (currentUser == null) return;

        int userId = currentUser.getUserId();

        // Get projects user is a member of
        List<Project> myProjects = ProjectDAO.getProjectsByUser(userId);

        // Clear existing cards before re-populating
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
     * Dynamically builds a project card for the dashboard project list.
     *
     * <p>Each card contains:</p>
     * <ul>
     *   <li>Project name + status badge + user's role badge</li>
     *   <li>Owner username</li>
     *   <li>Description (truncated to 120 characters)</li>
     *   <li>Progress bar with percentage</li>
     *   <li>View Details button</li>
     * </ul>
     *
     * @param project The project to build the card for
     * @return A styled VBox card ready to be added to {@link #projectsContainer}
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

        // Role badge — falls back to "MEMBER" if no role record found
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
     * Returns the CSS badge class for a given project status string.
     *
     * @param status The project status from the database
     * @return The corresponding CSS class name for the status badge
     */
    private String getStatusBadgeClass(String status) {
        return switch (status.toLowerCase()) {
            case "not started" -> "badge-not-started";
            case "in progress" -> "badge-in-progress";
            case "completed"   -> "badge-completed";
            case "published"   -> "badge-published";
            default            -> "badge-not-started";
        };
    }

    /**
     * Stores the selected project in {@link SelectedProject} and navigates
     * to the Project Details screen.
     *
     * @param project The project whose details should be displayed
     */
    private void handleViewProject(Project project) {
        SelectedProject.setProject(project);
        navigateTo("project-details.fxml", "Project Details", 1100, 700);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────────────────

    /** Already on Dashboard — refreshes the project list and stat cards */
    @FXML
    private void handleNavDashboard() {
        // Already on dashboard - refresh
        loadMyProjects();
        loadStats();
    }

    /** Navigates to the Browse Projects screen */
    @FXML
    private void handleNavBrowse() {
        navigateTo("browse-projects.fxml", "Browse Projects", 1100, 700);
    }

    /** Navigates to the Create Project screen */
    @FXML
    private void handleCreateProject() {
        navigateTo("create-project.fxml", "Create Project", 1100, 700);
    }

    /** Navigates to the Browse Projects screen (used for My Projects sidebar button) */
    @FXML
    private void handleNavMyProjects() {
        navigateTo("browse-projects.fxml", "My Projects", 1100, 700);
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
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TECHY | Project Manager - " + title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the active {@link Session} and navigates back to the Login screen.
     * Note: uses inline FXMLLoader rather than navigateTo() as login uses a
     * different scene size (600x500) and is a terminal navigation point.
     */
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
     * Displays a standard JavaFX information alert with no header.
     *
     * @param title   Title shown in the alert window's title bar
     * @param message Message body shown inside the alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}