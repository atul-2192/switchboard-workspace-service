package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.Exception.BadRequestException;
import com.SwitchBoard.WorkspaceService.Exception.ResourceNotFoundException;
import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.response.WorkspaceResponse;
import com.SwitchBoard.WorkspaceService.entity.Workspace;
import com.SwitchBoard.WorkspaceService.entity.WorkspaceAccess;
import com.SwitchBoard.WorkspaceService.entity.enums.WorkspaceType;
import com.SwitchBoard.WorkspaceService.repository.WorkspaceAccessRepository;
import com.SwitchBoard.WorkspaceService.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceImplTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceAccessRepository workspaceAccessRepository;

    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    private UUID workspaceId;
    private UUID userId;
    private Workspace workspace;
    private WorkspaceAccess workspaceAccess;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        userId = UUID.randomUUID();

        workspace = Workspace.builder()
                .name("Test Workspace")
                .description("Test Description")
                .workspaceType(WorkspaceType.DEFAULT)
                .ownerUserId(userId)
                .build();
        workspace.setId(workspaceId);
        workspace.setCreatedAt(Instant.now());
        workspace.setUpdatedAt(Instant.now());

        workspaceAccess = WorkspaceAccess.builder()
                .workspace(workspace)
                .userId(userId)
                .accessLevel(WorkspaceAccess.AccessLevel.ADMIN)
                .isActive(true)
                .build();
        workspaceAccess.setId(UUID.randomUUID());
    }

    @Test
    void getWorkspaceById_existingId_returnsWorkspace() {
        // Arrange
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceAccessRepository.findByWorkspaceIdAndIsActiveTrue(workspaceId))
                .thenReturn(Collections.singletonList(workspaceAccess));
        when(workspaceAccessRepository.countActiveUsersByWorkspaceId(workspaceId)).thenReturn(1L);

        // Act
        WorkspaceResponse response = workspaceService.getWorkspaceById(workspaceId);

        // Assert
        assertNotNull(response);
        assertEquals(workspaceId, response.getId());
        assertEquals("Test Workspace", response.getName());
        assertEquals("Test Description", response.getDescription());
        assertEquals(userId, response.getOwnerUserId());
        assertEquals(1, response.getUserAccessCount());

        verify(workspaceRepository, times(1)).findById(workspaceId);
    }

    @Test
    void getWorkspaceById_nonExistingId_throwsException() {
        // Arrange
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> workspaceService.getWorkspaceById(workspaceId));
        verify(workspaceRepository, times(1)).findById(workspaceId);
    }

    @Test
    void getWorkspacesByOwnerUserId_existingWorkspaces_returnsWorkspaces() {
        // Arrange
        List<Workspace> workspaces = Arrays.asList(workspace);
        when(workspaceRepository.findByOwnerUserId(userId)).thenReturn(workspaces);
        when(workspaceAccessRepository.findByWorkspaceIdAndIsActiveTrue(any()))
                .thenReturn(Collections.singletonList(workspaceAccess));
        when(workspaceAccessRepository.countActiveUsersByWorkspaceId(any())).thenReturn(1L);

        // Act
        List<WorkspaceResponse> responses = workspaceService.getWorkspacesByOwnerUserId(userId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Workspace", responses.get(0).getName());

        verify(workspaceRepository, times(1)).findByOwnerUserId(userId);
    }

    @Test
    void getWorkspacesByOwnerUserId_noExistingWorkspaces_createsDefaultWorkspaces() {
        // Arrange
        when(workspaceRepository.findByOwnerUserId(userId)).thenReturn(Collections.emptyList());

        Workspace defaultWorkspace = Workspace.builder()
                .name("Default")
                .workspaceType(WorkspaceType.DEFAULT)
                .ownerUserId(userId)
                .build();
        defaultWorkspace.setId(UUID.randomUUID());

        Workspace roadmapWorkspace = Workspace.builder()
                .name("Roadmap")
                .workspaceType(WorkspaceType.ROADMAP)
                .ownerUserId(userId)
                .build();
        roadmapWorkspace.setId(UUID.randomUUID());

        Workspace projectWorkspace = Workspace.builder()
                .name("Projects")
                .workspaceType(WorkspaceType.GROUP_PROJECT)
                .ownerUserId(userId)
                .build();
        projectWorkspace.setId(UUID.randomUUID());

        when(workspaceRepository.save(any(Workspace.class)))
                .thenReturn(defaultWorkspace)
                .thenReturn(roadmapWorkspace)
                .thenReturn(projectWorkspace);

        when(workspaceAccessRepository.findByWorkspaceIdAndIsActiveTrue(any()))
                .thenReturn(Collections.emptyList());
        when(workspaceAccessRepository.countActiveUsersByWorkspaceId(any())).thenReturn(0L);

        // Act
        List<WorkspaceResponse> responses = workspaceService.getWorkspacesByOwnerUserId(userId);

        // Assert
        assertNotNull(responses);
        assertEquals(3, responses.size());

        verify(workspaceRepository, times(1)).findByOwnerUserId(userId);
        verify(workspaceRepository, times(3)).save(any(Workspace.class));
    }

    @Test
    void getWorkspacesAccessibleByUser_returnsOwnedWorkspaces() {
        // Arrange
        List<Workspace> workspaces = Arrays.asList(workspace);
        when(workspaceRepository.findByOwnerUserId(userId)).thenReturn(workspaces);
        when(workspaceAccessRepository.findByWorkspaceIdAndIsActiveTrue(any()))
                .thenReturn(Collections.singletonList(workspaceAccess));
        when(workspaceAccessRepository.countActiveUsersByWorkspaceId(any())).thenReturn(1L);

        // Act
        List<WorkspaceResponse> responses = workspaceService.getWorkspacesAccessibleByUser(userId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());

        verify(workspaceRepository, times(1)).findByOwnerUserId(userId);
    }

    @Test
    void deleteWorkspace_existingId_deletesWorkspace() {
        // Arrange
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        doNothing().when(workspaceRepository).delete(workspace);

        // Act
        workspaceService.deleteWorkspace(workspaceId);

        // Assert
        verify(workspaceRepository, times(1)).findById(workspaceId);
        verify(workspaceRepository, times(1)).delete(workspace);
    }

    @Test
    void deleteWorkspace_nonExistingId_throwsException() {
        // Arrange
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> workspaceService.deleteWorkspace(workspaceId));
        verify(workspaceRepository, times(1)).findById(workspaceId);
        verify(workspaceRepository, never()).delete(any());
    }

    @Test
    void addUserToWorkspace_validRequest_addsUser() {
        // Arrange
        UUID newUserId = UUID.randomUUID();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceAccessRepository.findByWorkspaceIdAndUserId(workspaceId, newUserId))
                .thenReturn(Optional.empty());
        when(workspaceAccessRepository.save(any(WorkspaceAccess.class))).thenReturn(workspaceAccess);

        // Act
        workspaceService.addUserToWorkspace(workspaceId, newUserId, WorkspaceAccess.AccessLevel.WRITE);

        // Assert
        verify(workspaceRepository, times(1)).findById(workspaceId);
        verify(workspaceAccessRepository, times(1)).findByWorkspaceIdAndUserId(workspaceId, newUserId);
        verify(workspaceAccessRepository, times(1)).save(any(WorkspaceAccess.class));
    }

    @Test
    void addUserToWorkspace_nonExistingWorkspace_throwsException() {
        // Arrange
        UUID newUserId = UUID.randomUUID();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> workspaceService.addUserToWorkspace(workspaceId, newUserId, WorkspaceAccess.AccessLevel.WRITE));

        verify(workspaceRepository, times(1)).findById(workspaceId);
        verify(workspaceAccessRepository, never()).save(any());
    }

    @Test
    void addUserToWorkspace_userAlreadyHasAccess_throwsException() {
        // Arrange
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceAccessRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(Optional.of(workspaceAccess));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> workspaceService.addUserToWorkspace(workspaceId, userId, WorkspaceAccess.AccessLevel.WRITE));

        verify(workspaceRepository, times(1)).findById(workspaceId);
        verify(workspaceAccessRepository, times(1)).findByWorkspaceIdAndUserId(workspaceId, userId);
        verify(workspaceAccessRepository, never()).save(any());
    }

    @Test
    void removeUserFromWorkspace_validRequest_removesUser() {
        // Arrange
        when(workspaceRepository.existsById(workspaceId)).thenReturn(true);
        doNothing().when(workspaceAccessRepository).deleteByWorkspaceIdAndUserId(workspaceId, userId);

        // Act
        workspaceService.removeUserFromWorkspace(workspaceId, userId);

        // Assert
        verify(workspaceRepository, times(1)).existsById(workspaceId);
        verify(workspaceAccessRepository, times(1)).deleteByWorkspaceIdAndUserId(workspaceId, userId);
    }

    @Test
    void removeUserFromWorkspace_nonExistingWorkspace_throwsException() {
        // Arrange
        when(workspaceRepository.existsById(workspaceId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> workspaceService.removeUserFromWorkspace(workspaceId, userId));

        verify(workspaceRepository, times(1)).existsById(workspaceId);
        verify(workspaceAccessRepository, never()).deleteByWorkspaceIdAndUserId(any(), any());
    }

    @Test
    void updateUserAccessLevel_validRequest_updatesAccessLevel() {
        // Arrange
        when(workspaceAccessRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(Optional.of(workspaceAccess));
        when(workspaceAccessRepository.save(any(WorkspaceAccess.class))).thenReturn(workspaceAccess);

        // Act
        workspaceService.updateUserAccessLevel(workspaceId, userId, WorkspaceAccess.AccessLevel.read);

        // Assert
        verify(workspaceAccessRepository, times(1)).findByWorkspaceIdAndUserId(workspaceId, userId);
        verify(workspaceAccessRepository, times(1)).save(any(WorkspaceAccess.class));
    }

    @Test
    void updateUserAccessLevel_nonExistingAccess_throwsException() {
        // Arrange
        when(workspaceAccessRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> workspaceService.updateUserAccessLevel(workspaceId, userId, WorkspaceAccess.AccessLevel.read));

        verify(workspaceAccessRepository, times(1)).findByWorkspaceIdAndUserId(workspaceId, userId);
        verify(workspaceAccessRepository, never()).save(any());
    }

    @Test
    void getWorkspaceUsers_existingWorkspace_returnsUserIds() {
        // Arrange
        when(workspaceRepository.existsById(workspaceId)).thenReturn(true);
        when(workspaceAccessRepository.findByWorkspaceIdAndIsActiveTrue(workspaceId))
                .thenReturn(Arrays.asList(workspaceAccess));

        // Act
        List<UUID> userIds = workspaceService.getWorkspaceUsers(workspaceId);

        // Assert
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertEquals(userId, userIds.get(0));

        verify(workspaceRepository, times(1)).existsById(workspaceId);
        verify(workspaceAccessRepository, times(1)).findByWorkspaceIdAndIsActiveTrue(workspaceId);
    }

    @Test
    void getWorkspaceUsers_nonExistingWorkspace_throwsException() {
        // Arrange
        when(workspaceRepository.existsById(workspaceId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> workspaceService.getWorkspaceUsers(workspaceId));

        verify(workspaceRepository, times(1)).existsById(workspaceId);
        verify(workspaceAccessRepository, never()).findByWorkspaceIdAndIsActiveTrue(any());
    }

    @Test
    void activateWorkspace_noExistingWorkspaces_createsDefaultWorkspaces() {
        // Arrange
        when(workspaceRepository.findByOwnerUserId(userId)).thenReturn(Collections.emptyList());
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        // Act
        ApiResponse response = workspaceService.activateWorkspace(userId);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Default workspaces activated successfully", response.getMessage());

        verify(workspaceRepository, times(1)).findByOwnerUserId(userId);
        verify(workspaceRepository, times(3)).save(any(Workspace.class));
    }

    @Test
    void activateWorkspace_existingWorkspaces_skipsActivation() {
        // Arrange
        when(workspaceRepository.findByOwnerUserId(userId))
                .thenReturn(Arrays.asList(workspace));

        // Act
        ApiResponse response = workspaceService.activateWorkspace(userId);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("User already has workspaces", response.getMessage());

        verify(workspaceRepository, times(1)).findByOwnerUserId(userId);
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void getRoadmapWorkspaceByUserId_existingRoadmapWorkspace_returnsWorkspace() {
        // Arrange
        Workspace roadmapWorkspace = Workspace.builder()
                .name("Roadmap")
                .workspaceType(WorkspaceType.ROADMAP)
                .ownerUserId(userId)
                .build();
        roadmapWorkspace.setId(UUID.randomUUID());

        when(workspaceRepository.findByOwnerUserId(userId))
                .thenReturn(Arrays.asList(workspace, roadmapWorkspace));

        // Act
        Workspace result = workspaceService.getRoadmapWorkspaceByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(WorkspaceType.ROADMAP, result.getWorkspaceType());

        verify(workspaceRepository, times(1)).findByOwnerUserId(userId);
    }

    @Test
    void getRoadmapWorkspaceByUserId_noWorkspaces_createsDefaultAndReturnsRoadmap() {
        // Arrange
        when(workspaceRepository.findByOwnerUserId(userId)).thenReturn(Collections.emptyList());

        Workspace roadmapWorkspace = Workspace.builder()
                .name("Roadmap")
                .workspaceType(WorkspaceType.ROADMAP)
                .ownerUserId(userId)
                .build();
        roadmapWorkspace.setId(UUID.randomUUID());

        when(workspaceRepository.save(any(Workspace.class)))
                .thenReturn(workspace)
                .thenReturn(roadmapWorkspace)
                .thenReturn(workspace);

        // Act
        Workspace result = workspaceService.getRoadmapWorkspaceByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(WorkspaceType.ROADMAP, result.getWorkspaceType());

        verify(workspaceRepository, times(1)).findByOwnerUserId(userId);
        verify(workspaceRepository, times(3)).save(any(Workspace.class));
    }

    @Test
    void getRoadmapWorkspaceByUserId_noRoadmapWorkspace_throwsException() {
        // Arrange
        Workspace defaultWorkspace = Workspace.builder()
                .name("Default")
                .workspaceType(WorkspaceType.DEFAULT)
                .ownerUserId(userId)
                .build();
        defaultWorkspace.setId(UUID.randomUUID());

        when(workspaceRepository.findByOwnerUserId(userId))
                .thenReturn(Arrays.asList(defaultWorkspace));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> workspaceService.getRoadmapWorkspaceByUserId(userId));

        verify(workspaceRepository, times(1)).findByOwnerUserId(userId);
    }
}
