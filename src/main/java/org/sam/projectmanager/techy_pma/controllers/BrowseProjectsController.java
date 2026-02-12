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

public class BrowseProjectsController {

    @FXML private Label avatarLabel;
    @FXML private Label sidebarUsername;
    @FXML private Label sidebarEmail;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label resultsLabel;
    @FXML private VBox projectsContainer;
    @FXML private VBox emptyState;

    @FXML
    public void initialize() {
        loadUserInfo();
        setupFilterComboBox();
        loadAllProjects();
        // Trigger when Enter is pressed
        searchField.setOnAction(event -> handleSearch());
        // Trigger when filter selection changes
        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
    }

    private void loadUserInfo() {
        if (Session.getCurrentUser() == null) return;
        sidebarUsername.setText(Session.getCurrentUser().getUsername());
        sidebarEmail.setText(Session.getCurrentUser().getEmail());
        avatarLabel.setText(
                String.valueOf(Session.getCurrentUser().getUsername().charAt(0)).toUpperCase()
        );
    }

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

    private void loadAllProjects() {
        List<Project> projects = ProjectDAO.getAllProjects();
        displayProjects(projects);
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        String filter = filterComboBox.getValue();

        List<Project> projects;

        if (!searchTerm.isEmpty()) {
            projects = ProjectDAO.searchProjectsByName(searchTerm);
        } else {
            projects = ProjectDAO.getAllProjects();
        }

        // Apply status filter
        if (filter != null && !filter.equals("All")) {
            projects = projects.stream()
                    .filter(p -> p.getStatus().equals(filter))
                    .toList();
        }

        displayProjects(projects);
    }


    @FXML
    private void handleClear() {
        searchField.clear();
        filterComboBox.setValue("All");
        loadAllProjects();
    }

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

        // View button
        Button viewBtn = new Button("View Details →");
        viewBtn.getStyleClass().add("project-view-btn");
        viewBtn.setOnAction(e -> handleViewProject(project));

        // Join / Already Joined button
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

    private void handleViewProject(Project project) {
        // Store selected project for details screen
        SelectedProject.setProject(project);
        navigateTo("project-details.fxml", "Project Details", 1100, 700);
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
    @FXML private void handleNavMyProjects() { navigateTo("browse-projects.fxml", "Browse Projects", 1100, 700); }
    @FXML private void handleNavBrowse() { loadAllProjects(); }
    @FXML private void handleCreateProject() { navigateTo("create-project.fxml", "Create Project", 1100, 700); }

    @FXML
    private void handleLogout() {
        Session.clearSession();
        navigateTo("login.fxml", "Login", 600, 500);
    }

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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}