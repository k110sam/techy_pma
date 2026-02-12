package org.sam.projectmanager.techy_pma.models;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the relationship between a User and a Project,
 * including role and membership metadata.
 * This is a junction/association class for the many-to-many relationship
 * between Users and Projects.
 */
public class ProjectMember {

    // Allowed role values
    private static final Set<String> ALLOWED_ROLES = new HashSet<>(
            Arrays.asList("Owner", "Admin", "Member")
    );

    // Private fields
    private int id;                     // Primary key
    private int projectId;              // Foreign key to Project
    private int userId;                 // Foreign key to User
    private String role;                // Allowed values: Owner, Admin, Member
    private LocalDateTime joinedAt;     // Database-managed join timestamp

    /**
     * Constructor for creating a new membership (before saving to database).
     * Used when adding a user to a project.
     *
     * @param projectId the ID of the project
     * @param userId the ID of the user
     * @param role the role of the user in the project (Owner, Admin, or Member)
     */
    public ProjectMember(int projectId, int userId, String role) {
        this.projectId = projectId;
        this.userId = userId;
        setRole(role); // Use setter for validation
    }

    /**
     * Constructor for creating a membership object from database records.
     * Used by DAO layer when retrieving existing memberships.
     *
     * @param id the unique membership identifier
     * @param projectId the ID of the project
     * @param userId the ID of the user
     * @param role the role of the user in the project
     * @param joinedAt the timestamp when the user joined the project
     */
    public ProjectMember(int id, int projectId, int userId, String role, LocalDateTime joinedAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        setRole(role); // Use setter for validation
        this.joinedAt = joinedAt;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    /**
     * Sets the member's role with validation.
     *
     * @param role the role (must be one of: Owner, Admin, Member)
     * @throws IllegalArgumentException if role is not one of the allowed values
     */
    public void setRole(String role) {
        if (role == null || !ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException(
                    "Invalid role. Allowed values: " + ALLOWED_ROLES + ". Provided: " + role
            );
        }
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    /**
     * Returns a readable string representation of the ProjectMember object.
     *
     * @return string representation with all membership details
     */
    @Override
    public String toString() {
        return "ProjectMember{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", userId=" + userId +
                ", role='" + role + '\'' +
                ", joinedAt=" + joinedAt +
                '}';
    }

    /**
     * Checks equality based on id (primary key).
     *
     * @param o object to compare
     * @return true if memberships have the same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectMember that = (ProjectMember) o;
        return id == that.id;
    }

    /**
     * Generates hash code based on id.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Utility method to get all allowed role values.
     *
     * @return set of allowed role values
     */
    public static Set<String> getAllowedRoles() {
        return new HashSet<>(ALLOWED_ROLES);
    }

    /**
     * Utility method to check if the member is an Owner.
     *
     * @return true if role is Owner
     */
    public boolean isOwner() {
        return "Owner".equals(this.role);
    }

    /**
     * Utility method to check if the member is an Admin.
     *
     * @return true if role is Admin
     */
    public boolean isAdmin() {
        return "Admin".equals(this.role);
    }

    /**
     * Utility method to check if the member has administrative privileges.
     *
     * @return true if role is Owner or Admin
     */
    public boolean hasAdminPrivileges() {
        return isOwner() || isAdmin();
    }
}