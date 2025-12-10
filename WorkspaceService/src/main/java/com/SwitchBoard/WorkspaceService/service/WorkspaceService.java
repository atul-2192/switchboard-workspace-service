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
    
    List<WorkspaceResponse> getWorkspacesByVisibility(Workspace.WorkspaceVisibility visibility);
    
    List<WorkspaceResponse> searchWorkspacesByName(String name);
    
    WorkspaceResponse updateWorkspace(UUID id, WorkspaceCreateRequest request);
    
    void deleteWorkspace(UUID id);
    
//    Long getTaskCount(UUID workspaceId);
    
    Long getAssignmentCount(UUID workspaceId);
    
    // Workspace access management methods
    void addUserToWorkspace(UUID workspaceId, UUID userId, WorkspaceAccess.AccessLevel accessLevel);
    
    void removeUserFromWorkspace(UUID workspaceId, UUID userId);
    
    void updateUserAccessLevel(UUID workspaceId, UUID userId, WorkspaceAccess.AccessLevel accessLevel);
    
    List<UUID> getWorkspaceUsers(UUID workspaceId);
    
    boolean hasUserAccess(UUID workspaceId, UUID userId);
}