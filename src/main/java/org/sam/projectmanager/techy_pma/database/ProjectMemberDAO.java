package org.sam.projectmanager.techy_pma.database;

import org.sam.projectmanager.techy_pma.database.DatabaseManager;
import org.sam.projectmanager.techy_pma.models.ProjectMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for ProjectMember entity.
 * Handles all database operations for the project_members table.
 */
public class ProjectMemberDAO {

    /**
     * Add a member to a project
     * @param member The ProjectMember object to insert
     * @return The generated id, or -1 if failed
     */
    public static int addMember(ProjectMember member) {
        String sql = "INSERT INTO project_members (project_id, user_id, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, member.getProjectId());
            stmt.setInt(2, member.getUserId());
            stmt.setString(3, member.getRole());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        member.setId(id);
                        System.out.println("✓ Member added to project successfully");
                        return id;
                    }
                }
            }

        } catch (SQLException e) {
            // Check if it's a duplicate entry error
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Error: User is already a member of this project");
            } else {
                System.err.println("Error adding member: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return -1;
    }

    /**
     * Get all members of a specific project
     * @param projectId The project ID
     * @return List of ProjectMember objects
     */
    public static List<ProjectMember> getMembersByProject(int projectId) {
        List<ProjectMember> members = new ArrayList<>();
        String sql = "SELECT * FROM project_members WHERE project_id = ? ORDER BY joined_at ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapResultSetToProjectMember(rs));
                }
            }

            System.out.println("✓ Retrieved " + members.size() + " members for project " + projectId);

        } catch (SQLException e) {
            System.err.println("Error getting members by project: " + e.getMessage());
            e.printStackTrace();
        }

        return members;
    }

    /**
     * Get all projects a user is a member of
     * @param userId The user ID
     * @return List of ProjectMember objects
     */
    public static List<ProjectMember> getProjectsByUser(int userId) {
        List<ProjectMember> memberships = new ArrayList<>();
        String sql = "SELECT * FROM project_members WHERE user_id = ? ORDER BY joined_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    memberships.add(mapResultSetToProjectMember(rs));
                }
            }

            System.out.println("✓ User " + userId + " is a member of " + memberships.size() + " projects");

        } catch (SQLException e) {
            System.err.println("Error getting projects by user: " + e.getMessage());
            e.printStackTrace();
        }

        return memberships;
    }

    /**
     * Get a user's role in a specific project
     * @param projectId The project ID
     * @param userId The user ID
     * @return The user's role, or null if not a member
     */
    public static String getUserRole(int projectId, int userId) {
        String sql = "SELECT role FROM project_members WHERE project_id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting user role: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Check if a user is a member of a project
     * @param projectId The project ID
     * @param userId The user ID
     * @return true if user is a member, false otherwise
     */
    public static boolean isMember(int projectId, int userId) {
        String sql = "SELECT COUNT(*) FROM project_members WHERE project_id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking membership: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update a user's role in a project
     * @param projectId The project ID
     * @param userId The user ID
     * @param newRole The new role
     * @return true if update successful, false otherwise
     */
    public static boolean updateRole(int projectId, int userId, String newRole) {
        String sql = "UPDATE project_members SET role = ? WHERE project_id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newRole);
            stmt.setInt(2, projectId);
            stmt.setInt(3, userId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✓ User role updated to '" + newRole + "'");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error updating role: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Remove a user from a project
     * @param projectId The project ID
     * @param userId The user ID
     * @return true if removal successful, false otherwise
     */
    public static boolean removeMember(int projectId, int userId) {
        String sql = "DELETE FROM project_members WHERE project_id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✓ Member removed from project");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error removing member: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get count of members in a project
     * @param projectId The project ID
     * @return Number of members
     */
    public static int getMemberCount(int projectId) {
        String sql = "SELECT COUNT(*) FROM project_members WHERE project_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting member count: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Helper method to map a ResultSet row to a ProjectMember object
     * @param rs The ResultSet positioned at a valid row
     * @return ProjectMember object created from the row data
     * @throws SQLException if there's an error reading the ResultSet
     */
    private static ProjectMember mapResultSetToProjectMember(ResultSet rs) throws SQLException {
        return new ProjectMember(
                rs.getInt("id"),
                rs.getInt("project_id"),
                rs.getInt("user_id"),
                rs.getString("role"),
                rs.getTimestamp("joined_at").toLocalDateTime()
        );
    }
}