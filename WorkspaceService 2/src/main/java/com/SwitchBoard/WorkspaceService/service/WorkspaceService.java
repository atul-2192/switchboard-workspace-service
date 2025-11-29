package com.SwitchBoard.WorkspaceService.service;

import com.SwitchBoard.WorkspaceService.dto.request.WorkspaceCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.WorkspaceResponse;
import com.SwitchBoard.WorkspaceService.entity.Workspace;
import com.SwitchBoard.WorkspaceService.entity.WorkspaceAccess;
import java.util.List;
import java.util.UUID;

public interface WorkspaceService {

    WorkspaceResponse createWorkspace(WorkspaceCreateRequest request);
    
    WorkspaceResponse getWorkspaceById(UUID id);
    
    List<WorkspaceResponse> getWorkspacesByOwnerUserId(UUID ownerUserId);
    
    List<WorkspaceResponse> getWorkspacesAccessibleByUser(UUID userId);
    
    void deleteWorkspace(UUID id);
    
    // Workspace access management methods
    void addUserToWorkspace(UUID workspaceId, UUID userId, WorkspaceAccess.AccessLevel accessLevel);
    
    void removeUserFromWorkspace(UUID workspaceId, UUID userId);
    
    void updateUserAccessLevel(UUID workspaceId, UUID userId, WorkspaceAccess.AccessLevel accessLevel);
    
    List<UUID> getWorkspaceUsers(UUID workspaceId);
}