package org.sam.projectmanager.techy_pma.models;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a project within the application.
 * This class encapsulates project information including metadata, progress tracking, and status.
 */
public class Project {

    // Allowed status values
    private static final Set<String> ALLOWED_STATUSES = new HashSet<>(
            Arrays.asList("not started", "in progress", "completed", "published", "canceled")
    );

    // Private fields
    private int projectId;              // Primary key
    private String projectName;
    private String projectDescription;
    private int projectProgress;        // Range: 0-100
    private int createdBy;              // User ID (foreign key)
    private LocalDateTime createdAt;    // Database-managed timestamp
    private String status;              // Allowed values: not started, in progress, completed, published, canceled

    /**
     * Constructor for creating a new project (before database insertion).
     * Sets default values for projectProgress (0) and status ("not started").
     *
     * @param projectName the name of the project
     * @param projectDescription the description of the project
     * @param createdBy the user ID of the project creator
     */
    public Project(String projectName, String projectDescription, int createdBy) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.createdBy = createdBy;
        this.projectProgress = 0;
        this.status = "not started";
    }

    /**
     * Constructor for creating a project object from database records.
     * Used by DAO layer when retrieving existing projects.
     *
     * @param projectId the unique project identifier
     * @param projectName the name of the project
     * @param projectDescription the description of the project
     * @param projectProgress the progress percentage (0-100)
     * @param createdBy the user ID of the project creator
     * @param createdAt the timestamp when the project was created
     * @param status the current status of the project
     */
    public Project(int projectId, String projectName, String projectDescription,
                   int projectProgress, int createdBy, LocalDateTime createdAt, String status) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        setProjectProgress(projectProgress); // Use setter for validation
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        setStatus(status); // Use setter for validation
    }

    // Getters and Setters

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public int getProjectProgress() {
        return projectProgress;
    }

    /**
     * Sets the project progress with validation.
     *
     * @param projectProgress the progress percentage (must be between 0 and 100)
     * @throws IllegalArgumentException if progress is not between 0 and 100
     */
    public void setProjectProgress(int projectProgress) {
        if (projectProgress < 0 || projectProgress > 100) {
            throw new IllegalArgumentException(
                    "Project progress must be between 0 and 100. Provided: " + projectProgress
            );
        }
        this.projectProgress = projectProgress;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Sets the project status with validation.
     *
     * @param status the status (must be one of: not started, in progress, completed, published, canceled)
     * @throws IllegalArgumentException if status is not one of the allowed values
     */
    public void setStatus(String status) {
        if (status == null || !ALLOWED_STATUSES.contains(status.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid status. Allowed values: " + ALLOWED_STATUSES + ". Provided: " + status
            );
        }
        this.status = status.toLowerCase();
    }

    /**
     * Returns a readable string representation of the Project object.
     *
     * @return string representation with all project details
     */
    @Override
    public String toString() {
        return "Project{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                ", projectDescription='" + projectDescription + '\'' +
                ", projectProgress=" + projectProgress + "%" +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                '}';
    }

    /**
     * Checks equality based on projectId (primary key).
     *
     * @param o object to compare
     * @return true if projects have the same projectId
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return projectId == project.projectId;
    }

    /**
     * Generates hash code based on projectId.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }

    /**
     * Utility method to get all allowed status values.
     *
     * @return set of allowed status values
     */
    public static Set<String> getAllowedStatuses() {
        return new HashSet<>(ALLOWED_STATUSES);
    }
}