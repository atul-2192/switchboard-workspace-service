package com.SwitchBoard.WorkspaceService.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceAccessTest {

    @Test
    void builder_createsWorkspaceAccessWithDefaults() {
        // Arrange
        Workspace workspace = Workspace.builder()
                .name("Test Workspace")
                .ownerUserId(UUID.randomUUID())
                .build();
        UUID userId = UUID.randomUUID();

        // Act
        WorkspaceAccess access = WorkspaceAccess.builder()
                .workspace(workspace)
                .userId(userId)
                .build();

        // Assert
        assertNotNull(access);
        assertEquals(workspace, access.getWorkspace());
        assertEquals(userId, access.getUserId());
        assertEquals(WorkspaceAccess.AccessLevel.read, access.getAccessLevel());
        assertTrue(access.getIsActive());
    }

    @Test
    void builder_createsWorkspaceAccessWithCustomValues() {
        // Arrange
        Workspace workspace = Workspace.builder()
                .name("Test Workspace")
                .ownerUserId(UUID.randomUUID())
                .build();
        UUID userId = UUID.randomUUID();

        // Act
        WorkspaceAccess access = WorkspaceAccess.builder()
                .workspace(workspace)
                .userId(userId)
                .accessLevel(WorkspaceAccess.AccessLevel.ADMIN)
                .isActive(false)
                .build();

        // Assert
        assertNotNull(access);
        assertEquals(WorkspaceAccess.AccessLevel.ADMIN, access.getAccessLevel());
        assertFalse(access.getIsActive());
    }

    @Test
    void setters_updateFields() {
        // Arrange
        WorkspaceAccess access = new WorkspaceAccess();
        Workspace workspace = Workspace.builder()
                .name("Workspace")
                .ownerUserId(UUID.randomUUID())
                .build();
        UUID userId = UUID.randomUUID();

        // Act
        access.setWorkspace(workspace);
        access.setUserId(userId);
        access.setAccessLevel(WorkspaceAccess.AccessLevel.WRITE);
        access.setIsActive(true);

        // Assert
        assertEquals(workspace, access.getWorkspace());
        assertEquals(userId, access.getUserId());
        assertEquals(WorkspaceAccess.AccessLevel.WRITE, access.getAccessLevel());
        assertTrue(access.getIsActive());
    }

    @Test
    void accessLevelEnum_hasAllValues() {
        // Assert
        assertNotNull(WorkspaceAccess.AccessLevel.read);
        assertNotNull(WorkspaceAccess.AccessLevel.WRITE);
        assertNotNull(WorkspaceAccess.AccessLevel.ADMIN);
    }
}
