package com.SwitchBoard.WorkspaceService.controller;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.response.WorkspaceResponse;
import com.SwitchBoard.WorkspaceService.entity.WorkspaceAccess;
import com.SwitchBoard.WorkspaceService.entity.enums.WorkspaceType;
import com.SwitchBoard.WorkspaceService.service.WorkspaceService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceControllerTest {

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private WorkspaceController workspaceController;

    @Test
    void activateWorkspace_validUserId_returnsCreated() {
        // Arrange
        UUID userId = UUID.randomUUID();
        ApiResponse expectedResponse = ApiResponse.response("Workspace activated", null, "/api/v1/workspaces");
        when(workspaceService.activateWorkspace(userId)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse> response = workspaceController.activateWorkspace(userId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(workspaceService, times(1)).activateWorkspace(userId);
    }

    @Test
    void getWorkspaceById_existingId_returnsWorkspace() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        WorkspaceResponse expectedWorkspace = WorkspaceResponse.builder()
                .id(workspaceId)
                .name("Test Workspace")
                .workspaceType(WorkspaceType.DEFAULT)
                .build();
        when(workspaceService.getWorkspaceById(workspaceId)).thenReturn(expectedWorkspace);

        // Act
        ResponseEntity<WorkspaceResponse> response = workspaceController.getWorkspaceById(workspaceId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedWorkspace, response.getBody());
        assertEquals(workspaceId, response.getBody().getId());
        verify(workspaceService, times(1)).getWorkspaceById(workspaceId);
    }

    @Test
    void getWorkspacesByOwnerUserId_validUserId_returnsWorkspacesList() {
        // Arrange
        UUID userId = UUID.randomUUID();
        List<WorkspaceResponse> expectedWorkspaces = Arrays.asList(
                WorkspaceResponse.builder().id(UUID.randomUUID()).name("Workspace 1").build(),
                WorkspaceResponse.builder().id(UUID.randomUUID()).name("Workspace 2").build()
        );
        when(workspaceService.getWorkspacesByOwnerUserId(userId)).thenReturn(expectedWorkspaces);

        // Act
        ResponseEntity<List<WorkspaceResponse>> response = workspaceController.getWorkspacesByOwnerUserId(userId.toString());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedWorkspaces, response.getBody());
        verify(workspaceService, times(1)).getWorkspacesByOwnerUserId(userId);
    }

    @Test
    void getWorkspacesAccessibleByUser_validUserId_returnsWorkspacesList() {
        // Arrange
        UUID userId = UUID.randomUUID();
        List<WorkspaceResponse> expectedWorkspaces = Arrays.asList(
                WorkspaceResponse.builder().id(UUID.randomUUID()).name("Workspace 1").build(),
                WorkspaceResponse.builder().id(UUID.randomUUID()).name("Workspace 2").build(),
                WorkspaceResponse.builder().id(UUID.randomUUID()).name("Workspace 3").build()
        );
        when(httpRequest.getHeader("X-User-Id")).thenReturn(userId.toString());
        when(workspaceService.getWorkspacesAccessibleByUser(userId)).thenReturn(expectedWorkspaces);

        // Act
        ResponseEntity<List<WorkspaceResponse>> response = workspaceController.getWorkspacesAccessibleByUser(httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(workspaceService, times(1)).getWorkspacesAccessibleByUser(userId);
    }

    @Test
    void getWorkspacesAccessibleByUser_missingUserIdHeader_throwsException() {
        // Arrange
        when(httpRequest.getHeader("X-User-Id")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            workspaceController.getWorkspacesAccessibleByUser(httpRequest);
        });
        verify(workspaceService, never()).getWorkspacesAccessibleByUser(any());
    }

    @Test
    void deleteWorkspace_existingId_returnsOk() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/workspaces/" + workspaceId);
        doNothing().when(workspaceService).deleteWorkspace(workspaceId);

        // Act
        ResponseEntity<ApiResponse> response = workspaceController.deleteWorkspace(workspaceId, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Workspace deleted successfully", response.getBody().getMessage());
        verify(workspaceService, times(1)).deleteWorkspace(workspaceId);
    }

    @Test
    void addUserToWorkspace_validInputs_returnsCreated() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkspaceAccess.AccessLevel accessLevel = WorkspaceAccess.AccessLevel.WRITE;
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/workspaces/" + workspaceId + "/users/" + userId);
        doNothing().when(workspaceService).addUserToWorkspace(workspaceId, userId, accessLevel);

        // Act
        ResponseEntity<ApiResponse> response = workspaceController.addUserToWorkspace(
                workspaceId, userId, accessLevel, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User added to workspace successfully", response.getBody().getMessage());
        verify(workspaceService, times(1)).addUserToWorkspace(workspaceId, userId, accessLevel);
    }

    @Test
    void addUserToWorkspace_adminAccess_addsUser() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkspaceAccess.AccessLevel accessLevel = WorkspaceAccess.AccessLevel.ADMIN;
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/workspaces");
        doNothing().when(workspaceService).addUserToWorkspace(workspaceId, userId, accessLevel);

        // Act
        ResponseEntity<ApiResponse> response = workspaceController.addUserToWorkspace(
                workspaceId, userId, accessLevel, httpRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(workspaceService, times(1)).addUserToWorkspace(workspaceId, userId, accessLevel);
    }

    @Test
    void removeUserFromWorkspace_validInputs_returnsOk() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/workspaces/" + workspaceId + "/users/" + userId);
        doNothing().when(workspaceService).removeUserFromWorkspace(workspaceId, userId);

        // Act
        ResponseEntity<ApiResponse> response = workspaceController.removeUserFromWorkspace(
                workspaceId, userId, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User removed from workspace successfully", response.getBody().getMessage());
        verify(workspaceService, times(1)).removeUserFromWorkspace(workspaceId, userId);
    }

    @Test
    void updateUserAccessLevel_validInputs_returnsOk() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkspaceAccess.AccessLevel newAccessLevel = WorkspaceAccess.AccessLevel.ADMIN;
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/workspaces/" + workspaceId);
        doNothing().when(workspaceService).updateUserAccessLevel(workspaceId, userId, newAccessLevel);

        // Act
        ResponseEntity<ApiResponse> response = workspaceController.updateUserAccessLevel(
                workspaceId, userId, newAccessLevel, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User access level updated successfully", response.getBody().getMessage());
        verify(workspaceService, times(1)).updateUserAccessLevel(workspaceId, userId, newAccessLevel);
    }

    @Test
    void updateUserAccessLevel_downgradeToRead_updatesAccess() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkspaceAccess.AccessLevel newAccessLevel = WorkspaceAccess.AccessLevel.read;
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/workspaces");
        doNothing().when(workspaceService).updateUserAccessLevel(workspaceId, userId, newAccessLevel);

        // Act
        ResponseEntity<ApiResponse> response = workspaceController.updateUserAccessLevel(
                workspaceId, userId, newAccessLevel, httpRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(workspaceService, times(1)).updateUserAccessLevel(workspaceId, userId, newAccessLevel);
    }

    @Test
    void getWorkspaceUsers_validWorkspaceId_returnsUserList() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        List<UUID> expectedUsers = Arrays.asList(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        when(workspaceService.getWorkspaceUsers(workspaceId)).thenReturn(expectedUsers);

        // Act
        ResponseEntity<List<UUID>> response = workspaceController.getWorkspaceUsers(workspaceId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals(expectedUsers, response.getBody());
        verify(workspaceService, times(1)).getWorkspaceUsers(workspaceId);
    }

    @Test
    void getWorkspaceUsers_emptyWorkspace_returnsEmptyList() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        when(workspaceService.getWorkspaceUsers(workspaceId)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<UUID>> response = workspaceController.getWorkspaceUsers(workspaceId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(workspaceService, times(1)).getWorkspaceUsers(workspaceId);
    }
}
