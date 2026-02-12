package org.sam.projectmanager.techy_pma.utils;

import org.sam.projectmanager.techy_pma.models.Project;

/**
 * Helper class to pass selected project between screens
 */
public class SelectedProject {
    private static Project project;

    public static void setProject(Project p) { project = p; }
    public static Project getProject() { return project; }
    public static void clear() { project = null; }
}