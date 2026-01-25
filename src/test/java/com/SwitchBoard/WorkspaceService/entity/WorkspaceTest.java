package com.SwitchBoard.WorkspaceService.entity;

import com.SwitchBoard.WorkspaceService.entity.enums.WorkspaceType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceTest {

    @Test
    void builder_createsWorkspaceWithFields() {
        // Arrange
        UUID ownerId = UUID.randomUUID();

        // Act
        Workspace workspace = Workspace.builder()
                .name("My Workspace")
                .description("Test description")
                .workspaceType(WorkspaceType.DEFAULT)
                .ownerUserId(ownerId)
                .build();

        // Assert
        assertNotNull(workspace);
        assertEquals("My Workspace", workspace.getName());
        assertEquals("Test description", workspace.getDescription());
        assertEquals(WorkspaceType.DEFAULT, workspace.getWorkspaceType());
        assertEquals(ownerId, workspace.getOwnerUserId());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        // Arrange
        Workspace workspace = new Workspace();
        UUID ownerId = UUID.randomUUID();

        // Act
        workspace.setName("Updated Name");
        workspace.setDescription("Updated Description");
        workspace.setWorkspaceType(WorkspaceType.ROADMAP);
        workspace.setOwnerUserId(ownerId);

        // Assert
        assertEquals("Updated Name", workspace.getName());
        assertEquals("Updated Description", workspace.getDescription());
        assertEquals(WorkspaceType.ROADMAP, workspace.getWorkspaceType());
        assertEquals(ownerId, workspace.getOwnerUserId());
    }

    @Test
    void collections_initializeCorrectly() {
        // Act
        Workspace workspace = Workspace.builder().build();

        // Assert
        assertNotNull(workspace.getWorkspaceAccess());
        assertNotNull(workspace.getAssignments());
        assertTrue(workspace.getWorkspaceAccess() instanceof HashSet);
        assertTrue(workspace.getAssignments() instanceof HashSet);
    }

    @Test
    void noArgsConstructor_createsInstance() {
        // Act
        Workspace workspace = new Workspace();

        // Assert
        assertNotNull(workspace);
    }

    @Test
    void allArgsConstructor_createsInstance() {
        // Arrange
        UUID ownerId = UUID.randomUUID();

        // Act
        Workspace workspace = new Workspace(
                "Name",
                "Description",
                WorkspaceType.GROUP_PROJECT,
                ownerId,
                new HashSet<>(),
                new HashSet<>()
        );

        // Assert
        assertNotNull(workspace);
        assertEquals("Name", workspace.getName());
        assertEquals(WorkspaceType.GROUP_PROJECT, workspace.getWorkspaceType());
    }
}
