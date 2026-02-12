package org.sam.projectmanager.techy_pma.database;

import org.sam.projectmanager.techy_pma.database.DatabaseManager;
import org.sam.projectmanager.techy_pma.models.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Project entity.
 * Handles all database operations for the projects table.
 */
public class ProjectDAO {

    /**
     * Insert a new project into the database
     * @param project The project object to insert
     * @return The generated project_id, or -1 if failed
     */
    public static int insertProject(Project project) {
        String sql = "INSERT INTO projects (project_name, project_description, project_progress, created_by, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters
            stmt.setString(1, project.getProjectName());
            stmt.setString(2, project.getProjectDescription());
            stmt.setInt(3, project.getProjectProgress());
            stmt.setInt(4, project.getCreatedBy());
            stmt.setString(5, project.getStatus());

            // Execute insert
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Get the generated project_id
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int projectId = generatedKeys.getInt(1);
                        project.setProjectId(projectId);  // Update the project object with the ID
                        System.out.println("✓ Project inserted successfully with ID: " + projectId);
                        return projectId;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error inserting project: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;  // Failed
    }

    /**
     * Get a project by its ID
     * @param projectId The project ID to search for
     * @return Project object if found, null otherwise
     */
    public static Project getProjectById(int projectId) {
        String sql = "SELECT * FROM projects WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProject(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting project by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get all projects from the database
     * @return List of all projects
     */
    public static List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }

            System.out.println("✓ Retrieved " + projects.size() + " projects");

        } catch (SQLException e) {
            System.err.println("Error getting all projects: " + e.getMessage());
            e.printStackTrace();
        }

        return projects;
    }

    /**
     * Get all projects created by a specific user
     * @param userId The user ID
     * @return List of projects created by the user
     */
    public static List<Project> getProjectsCreatedByUser(int userId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE created_by = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapResultSetToProject(rs));
                }
            }

            System.out.println("✓ Retrieved " + projects.size() + " projects created by user " + userId);

        } catch (SQLException e) {
            System.err.println("Error getting projects by user: " + e.getMessage());
            e.printStackTrace();
        }

        return projects;
    }

    /**
     * Get all projects a user is a member of (including ones they created)
     * @param userId The user ID
     * @return List of projects the user belongs to
     */
    public static List<Project> getProjectsByUser(int userId) {
        List<Project> projects = new ArrayList<>();
        String sql = """
            SELECT DISTINCT p.* FROM projects p
            INNER JOIN project_members pm ON p.project_id = pm.project_id
            WHERE pm.user_id = ?
            ORDER BY p.created_at DESC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapResultSetToProject(rs));
                }
            }

            System.out.println("✓ Retrieved " + projects.size() + " projects for user " + userId);

        } catch (SQLException e) {
            System.err.println("Error getting projects for user: " + e.getMessage());
            e.printStackTrace();
        }

        return projects;
    }

    /**
     * Update a project's information
     * @param project The project object with updated information
     * @return true if update successful, false otherwise
     */
    public static boolean updateProject(Project project) {
        String sql = "UPDATE projects SET project_name = ?, project_description = ?, project_progress = ?, status = ? WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, project.getProjectName());
            stmt.setString(2, project.getProjectDescription());
            stmt.setInt(3, project.getProjectProgress());
            stmt.setString(4, project.getStatus());
            stmt.setInt(5, project.getProjectId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✓ Project updated successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error updating project: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update only the progress of a project
     * @param projectId The project ID
     * @param progress The new progress value (0-100)
     * @return true if update successful, false otherwise
     */
    public static boolean updateProgress(int projectId, int progress) {
        String sql = "UPDATE projects SET project_progress = ? WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Validate progress
            if (progress < 0) progress = 0;
            if (progress > 100) progress = 100;

            stmt.setInt(1, progress);
            stmt.setInt(2, projectId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✓ Project progress updated to " + progress + "%");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error updating progress: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update only the status of a project
     * @param projectId The project ID
     * @param status The new status ("not started", "in progress", "completed", "published")
     * @return true if update successful, false otherwise
     */
    public static boolean updateStatus(int projectId, String status) {
        String sql = "UPDATE projects SET status = ? WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, projectId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✓ Project status updated to '" + status + "'");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error updating status: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Delete a project from the database
     * @param projectId The ID of the project to delete
     * @return true if deletion successful, false otherwise
     */
    public static boolean deleteProject(int projectId) {
        String sql = "DELETE FROM projects WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✓ Project deleted successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting project: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Search projects by name (case-insensitive partial match)
     * @param searchTerm The search term
     * @return List of projects matching the search
     */
    public static List<Project> searchProjectsByName(String searchTerm) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE project_name LIKE ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapResultSetToProject(rs));
                }
            }

            System.out.println("✓ Found " + projects.size() + " projects matching '" + searchTerm + "'");

        } catch (SQLException e) {
            System.err.println("Error searching projects: " + e.getMessage());
            e.printStackTrace();
        }

        return projects;
    }

    /**
     * Get projects by status
     * @param status The status to filter by
     * @return List of projects with the specified status
     */
    public static List<Project> getProjectsByStatus(String status) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE status = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapResultSetToProject(rs));
                }
            }

            System.out.println("✓ Retrieved " + projects.size() + " projects with status '" + status + "'");

        } catch (SQLException e) {
            System.err.println("Error getting projects by status: " + e.getMessage());
            e.printStackTrace();
        }

        return projects;
    }

    /**
     * Helper method to map a ResultSet row to a Project object
     * @param rs The ResultSet positioned at a valid row
     * @return Project object created from the row data
     * @throws SQLException if there's an error reading the ResultSet
     */
    private static Project mapResultSetToProject(ResultSet rs) throws SQLException {
        return new Project(
                rs.getInt("project_id"),
                rs.getString("project_name"),
                rs.getString("project_description"),
                rs.getInt("project_progress"),
                rs.getInt("created_by"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("status")
        );
    }
}