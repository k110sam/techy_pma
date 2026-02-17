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
import java.util.List;

/**
 * Controller for the Browse Projects screen.
 *
 * <p>Responsible for displaying all available projects in the system,
 * allowing users to search by project name, filter by status, join projects
 * they are not yet a member of, and navigate to full project details.</p>
 *
 * <p>This controller is bound to {@code browse-projects.fxml} via the
 * {@code fx:controller} attribute.</p>
 *
 * <p><b>Navigation sources:</b> Dashboard sidebar, My Projects sidebar button</p>
 * <p><b>Navigation targets:</b> Project Details, Dashboard, Create Project, Login</p>
 */
public class BrowseProjectsController {

    // ─── SIDEBAR FIELDS ───

    /** First letter of the logged-in user's username, displayed in the avatar circle */
    @FXML private Label avatarLabel;

    /** Username of the logged-in user displayed in the sidebar profile block */
    @FXML private Label sidebarUsername;

    /** Email of the logged-in user displayed in the sidebar profile block */
    @FXML private Label sidebarEmail;

    // ─── SEARCH & FILTER FIELDS ───

    /** Text input for searching projects by name (supports partial, case-insensitive match via SQL LIKE) */
    @FXML private TextField searchField;

    /** Dropdown for filtering projects by status: All, not started, in progress, completed, published */
    @FXML private ComboBox<String> filterComboBox;

    /** Shows the number of projects currently displayed e.g. "5 project(s) found" */
    @FXML private Label resultsLabel;

    // ─── CONTENT FIELDS ───

    /** Dynamic container populated with project cards at runtime */
    @FXML private VBox projectsContainer;

    /** Empty state panel — shown only when no projects match the current search or filter */
    @FXML private VBox emptyState;

    // ─────────────────────────────────────────────────────────────────────────
    // INITIALIZATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called automatically by JavaFX after all {@code @FXML} fields have been injected.
     *
     * <p>Performs the following setup in order:</p>
     * <ol>
     *   <li>Populates the sidebar with the current user's info</li>
     *   <li>Populates the status filter dropdown with available options</li>
     *   <li>Loads and displays all projects from the database</li>
     *   <li>Attaches an Enter key listener to the search field</li>
     *   <li>Attaches a change listener to auto-search when the filter selection changes</li>
     * </ol>
     */
    @FXML
    public void initialize() {
        loadUserInfo();
        setupFilterComboBox();
        loadAllProjects();
        // Trigger search when the user presses Enter inside the search field
        searchField.setOnAction(event -> handleSearch());
        // Automatically re-run search whenever the status filter dropdown value changes
        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SETUP HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads the current user's information from the active {@link Session}
     * and populates the sidebar avatar letter, username label, and email label.
     *
     * <p>Does nothing if no user is currently logged in (null guard).</p>
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
     * Populates the status filter {@link ComboBox} with all available project statuses.
     * Sets "All" as the default value so all projects are shown on first load.
     */
    private void setupFilterComboBox() {
        filterComboBox.getItems().addAll(
                "All",
                "not started",
                "in progress",
                "completed",
                "published"
        );
        filterComboBox.setValue("All");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATA LOADING
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fetches all projects from the database via {@link ProjectDAO#getAllProjects()}
     * and passes them to {@link #displayProjects(List)} for rendering.
     *
     * <p>Called on first load and when the Clear button is pressed.</p>
     */
    private void loadAllProjects() {
        List<Project> projects = ProjectDAO.getAllProjects();
        displayProjects(projects);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EVENT HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles the Search button click, Enter key press in the search field,
     * and automatic re-search triggered by filter dropdown value changes.
     *
     * <p>Search logic:</p>
     * <ol>
     *   <li>If the search field is not empty, query projects by name via {@link ProjectDAO#searchProjectsByName}</li>
     *   <li>If the search field is empty, fetch all projects</li>
     *   <li>If a specific status is selected (not "All"), narrow results using a stream filter</li>
     *   <li>Pass the final filtered list to {@link #displayProjects(List)}</li>
     * </ol>
     */
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        String filter = filterComboBox.getValue();

        List<Project> projects;

        // Use name-based search if a term is entered, otherwise load everything
        if (!searchTerm.isEmpty()) {
            projects = ProjectDAO.searchProjectsByName(searchTerm);
        } else {
            projects = ProjectDAO.getAllProjects();
        }

        // Apply status filter on top of search results.
        // "All" means no status filter — return everything from the name search.
        if (filter != null && !filter.equals("All")) {
            projects = projects.stream()
                    .filter(p -> p.getStatus().equals(filter))
                    .toList();
        }

        displayProjects(projects);
    }

    /**
     * Handles the Clear button click.
     * Resets the search field text and status filter dropdown to their defaults,
     * then reloads all projects from the database.
     */
    @FXML
    private void handleClear() {
        searchField.clear();
        filterComboBox.setValue("All");
        loadAllProjects();
    }

    /**
     * Renders a list of projects into the {@link #projectsContainer} VBox.
     *
     * <p>If the list is empty:</p>
     * <ul>
     *   <li>Shows the {@link #emptyState} panel</li>
     *   <li>Updates {@link #resultsLabel} to "0 projects found"</li>
     * </ul>
     *
     * <p>If the list has items:</p>
     * <ul>
     *   <li>Hides the empty state panel</li>
     *   <li>Updates {@link #resultsLabel} with the project count</li>
     *   <li>Creates and appends a project card for each project via {@link #createBrowseCard(Project)}</li>
     * </ul>
     *
     * @param projects The list of projects to render
     */
    private void displayProjects(List<Project> projects) {
        projectsContainer.getChildren().clear();

        if (projects.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            resultsLabel.setText("0 projects found");
        } else {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            resultsLabel.setText(projects.size() + " project(s) found");

            for (Project project : projects) {
                VBox card = createBrowseCard(project);
                projectsContainer.getChildren().add(card);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARD BUILDER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Dynamically builds a styled project card UI element for the browse list.
     *
     * <p>Each card contains:</p>
     * <ul>
     *   <li><b>Top row:</b> Project name + status badge</li>
     *   <li><b>Owner row:</b> Username of the project creator</li>
     *   <li><b>Description:</b> Truncated to 150 characters if longer</li>
     *   <li><b>Progress row:</b> Progress bar + percentage label</li>
     *   <li><b>Members row:</b> Total member count for the project</li>
     *   <li><b>Bottom row:</b> "View Details" button + either "Join Project"
     *       or a disabled "✓ Joined" button depending on membership status</li>
     * </ul>
     *
     * @param project The {@link Project} object to build the card for
     * @return A fully assembled {@link VBox} card ready to be added to {@link #projectsContainer}
     */
    private VBox createBrowseCard(Project project) {
        VBox card = new VBox(12);
        card.getStyleClass().add("project-card");

        int currentUserId = Session.getCurrentUserId();
        boolean isMember = ProjectMemberDAO.isMember(project.getProjectId(), currentUserId);

        // ─── TOP ROW ───
        HBox topRow = new HBox(10);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(project.getProjectName());
        nameLabel.getStyleClass().add("project-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label statusBadge = new Label(project.getStatus().toUpperCase());
        statusBadge.getStyleClass().addAll("badge", getStatusBadgeClass(project.getStatus()));

        topRow.getChildren().addAll(nameLabel, statusBadge);

        // ─── OWNER ───
        User owner = UserDAO.getUserById(project.getCreatedBy());
        String ownerName = owner != null ? owner.getUsername() : "Unknown";
        Label ownerLabel = new Label("⊙  Created by: " + ownerName);
        ownerLabel.getStyleClass().add("project-owner");

        // ─── DESCRIPTION ───
        String desc = project.getProjectDescription();
        if (desc == null || desc.isEmpty()) desc = "No description provided.";
        if (desc.length() > 150) desc = desc.substring(0, 150) + "...";
        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("project-description");
        descLabel.setWrapText(true);

        // ─── PROGRESS ───
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

        // ─── MEMBER COUNT ───
        int memberCount = ProjectMemberDAO.getMemberCount(project.getProjectId());
        Label membersLabel = new Label("◎  " + memberCount + " member(s)");
        membersLabel.getStyleClass().add("project-owner");

        // ─── BOTTOM ROW: Actions ───
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // View button — always visible regardless of membership
        Button viewBtn = new Button("View Details →");
        viewBtn.getStyleClass().add("project-view-btn");
        viewBtn.setOnAction(e -> handleViewProject(project));

        // Show "✓ Joined" (disabled) if already a member, otherwise show active "Join Project" button
        if (isMember) {
            Button joinedBtn = new Button("✓ Joined");
            joinedBtn.getStyleClass().add("joined-button");
            joinedBtn.setDisable(true);
            bottomRow.getChildren().addAll(viewBtn, joinedBtn);
        } else {
            Button joinBtn = new Button("⊕ Join Project");
            joinBtn.getStyleClass().add("join-button");
            joinBtn.setOnAction(e -> handleJoinProject(project, joinBtn));
            bottomRow.getChildren().addAll(viewBtn, joinBtn);
        }

        card.getChildren().addAll(topRow, ownerLabel, descLabel, progressRow, membersLabel, bottomRow);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JOIN & VIEW HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles the Join Project button click for a specific project card.
     *
     * <p>Join logic:</p>
     * <ol>
     *   <li>Re-checks membership via {@link ProjectMemberDAO#isMember} as a safety guard</li>
     *   <li>Creates a new {@link ProjectMember} with role "Member"</li>
     *   <li>Inserts the membership record via {@link ProjectMemberDAO#addMember}</li>
     *   <li>On success, updates the button text and style to reflect joined state</li>
     *   <li>On failure, shows an error alert</li>
     * </ol>
     *
     * @param project The project the user wants to join
     * @param joinBtn The Join button on the card, updated in-place on success
     */
    private void handleJoinProject(Project project, Button joinBtn) {
        int userId = Session.getCurrentUserId();
        int projectId = project.getProjectId();

        // Check if already a member
        if (ProjectMemberDAO.isMember(projectId, userId)) {
            showAlert("Already Joined", "You are already a member of this project!");
            return;
        }

        // Add as member
        ProjectMember membership = new ProjectMember(projectId, userId, "Member");
        int result = ProjectMemberDAO.addMember(membership);

        if (result > 0) {
            // Update button to show joined
            joinBtn.setText("✓ Joined");
            joinBtn.getStyleClass().remove("join-button");
            joinBtn.getStyleClass().add("joined-button");
            joinBtn.setDisable(true);

            System.out.println("✓ Joined project: " + project.getProjectName());
            showAlert("Success!", "You have successfully joined: " + project.getProjectName());
        } else {
            showAlert("Error", "Failed to join project. Please try again.");
        }
    }

    /**
     * Handles the View Details button click for a project card.
     *
     * <p>Stores the selected project in {@link SelectedProject} so that
     * {@code ProjectDetailsController} can retrieve it after navigation,
     * then navigates to the Project Details screen.</p>
     *
     * @param project The project whose details should be displayed
     */
    private void handleViewProject(Project project) {
        // Store selected project for details screen
        SelectedProject.setProject(project);
        navigateTo("project-details.fxml", "Project Details", 1100, 700);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITY HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the CSS style class name for a status badge based on the project's status string.
     *
     * <p>Mapped values:</p>
     * <ul>
     *   <li>{@code "not started"} → {@code "badge-not-started"} (grey)</li>
     *   <li>{@code "in progress"} → {@code "badge-in-progress"} (blue)</li>
     *   <li>{@code "completed"}   → {@code "badge-completed"}   (green)</li>
     *   <li>{@code "published"}   → {@code "badge-published"}   (orange)</li>
     *   <li>Any unknown value     → {@code "badge-not-started"} (default fallback)</li>
     * </ul>
     *
     * @param status The project status string from the database
     * @return The corresponding CSS class name for the badge
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

    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────────────────

    /** Navigates to the Dashboard screen */
    @FXML private void handleNavDashboard() { navigateTo("dashboard.fxml", "Dashboard", 1100, 700); }

    /** Navigates to the Browse Projects screen (reloads current screen) */
    @FXML private void handleNavMyProjects() { navigateTo("browse-projects.fxml", "Browse Projects", 1100, 700); }

    /** Refreshes the projects list by reloading all projects from the database */
    @FXML private void handleNavBrowse() { loadAllProjects(); }

    /** Navigates to the Create Project screen */
    @FXML private void handleCreateProject() { navigateTo("create-project.fxml", "Create Project", 1100, 700); }

    /**
     * Logs the current user out by clearing the {@link Session},
     * then navigates back to the Login screen.
     */
    @FXML
    private void handleLogout() {
        Session.clearSession();
        navigateTo("login.fxml", "Login", 600, 500);
    }

    /**
     * Generic navigation helper that loads a new FXML screen and replaces
     * the current scene on the existing {@link Stage}.
     *
     * <p>The FXML file path is automatically prefixed with the full resource
     * package path so callers only need to pass the filename.</p>
     *
     * @param fxmlFile The FXML filename only e.g. {@code "dashboard.fxml"}
     * @param title    The window title to display in the title bar
     * @param width    The width of the new scene in pixels
     * @param height   The height of the new scene in pixels
     */
    private void navigateTo(String fxmlFile, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/org/sam/projectmanager/techy_pma/fxml/" + fxmlFile)
            );
            Scene scene = new Scene(loader.load(), width, height);
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("TECHY | Project Manager - " + title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays a standard JavaFX information alert dialog with no header.
     *
     * @param title   The title shown in the alert window's title bar
     * @param message The message body shown inside the alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}