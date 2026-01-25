package com.SwitchBoard.WorkspaceService.dto.response;

import com.SwitchBoard.WorkspaceService.entity.enums.WorkspaceType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceResponseTest {

    @Test
    void builder_createsWorkspaceResponseWithAllFields() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        // Act
        WorkspaceResponse response = WorkspaceResponse.builder()
                .id(workspaceId)
                .name("Workspace Name")
                .description("Description")
                .workspaceType(WorkspaceType.DEFAULT)
                .ownerUserId(ownerId)
                .userAccessCount(10)
                .build();

        // Assert
        assertNotNull(response);
        assertEquals(workspaceId, response.getId());
        assertEquals("Workspace Name", response.getName());
        assertEquals("Description", response.getDescription());
        assertEquals(WorkspaceType.DEFAULT, response.getWorkspaceType());
        assertEquals(ownerId, response.getOwnerUserId());
        assertEquals(10, response.getUserAccessCount());
    }

    @Test
    void setters_updateFields() {
        // Arrange
        WorkspaceResponse response = new WorkspaceResponse();
        UUID workspaceId = UUID.randomUUID();

        // Act
        response.setId(workspaceId);
        response.setName("Updated Name");
        response.setWorkspaceType(WorkspaceType.ROADMAP);
        response.setUserAccessCount(5);

        // Assert
        assertEquals(workspaceId, response.getId());
        assertEquals("Updated Name", response.getName());
        assertEquals(WorkspaceType.ROADMAP, response.getWorkspaceType());
        assertEquals(5, response.getUserAccessCount());
    }

    @Test
    void noArgsConstructor_createsEmptyInstance() {
        // Act
        WorkspaceResponse response = new WorkspaceResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getName());
    }
}
