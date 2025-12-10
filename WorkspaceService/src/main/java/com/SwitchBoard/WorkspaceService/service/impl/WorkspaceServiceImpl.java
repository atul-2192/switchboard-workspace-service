package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.config.Constant;
import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.response.WorkspaceResponse;
import com.SwitchBoard.WorkspaceService.entity.Assignment;
import com.SwitchBoard.WorkspaceService.entity.Workspace;
import com.SwitchBoard.WorkspaceService.entity.WorkspaceAccess;
import com.SwitchBoard.WorkspaceService.entity.enums.WorkspaceType;
import com.SwitchBoard.WorkspaceService.repository.WorkspaceRepository;
import com.SwitchBoard.WorkspaceService.repository.WorkspaceAccessRepository;
import com.SwitchBoard.WorkspaceService.service.WorkspaceService;
import com.SwitchBoard.WorkspaceService.Exception.ResourceNotFoundException;
import com.SwitchBoard.WorkspaceService.Exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.SwitchBoard.WorkspaceService.config.Constant.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceAccessRepository workspaceAccessRepository;


    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID id) {
        log.info("WorkspaceServiceImpl :: getWorkspaceById :: Started fetching workspace :: {}", id);

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("WorkspaceServiceImpl :: getWorkspaceById :: Workspace not found :: {}", id);
                    return new ResourceNotFoundException("Workspace not found with ID: " + id);
                });

        log.info("WorkspaceServiceImpl :: getWorkspaceById :: Successfully retrieved workspace :: {} :: Name: {}", 
                id, workspace.getName());
        return convertToWorkspaceResponse(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesByOwnerUserId(UUID ownerUserId) {
        log.info("WorkspaceServiceImpl :: getWorkspacesByOwnerUserId :: Started fetching workspaces for user :: {}", ownerUserId);

        List<Workspace> workspaces = workspaceRepository.findByOwnerUserId(ownerUserId);
        log.debug("WorkspaceServiceImpl :: getWorkspacesByOwnerUserId :: Found {} existing workspaces for user :: {}", 
                workspaces.size(), ownerUserId);
        
        if (workspaces.isEmpty()) {
            log.info("WorkspaceServiceImpl :: getWorkspacesByOwnerUserId :: No workspaces found, creating default workspaces for user :: {}", ownerUserId);
            workspaces = createDefaultWorkspaces(ownerUserId);
        }
        
        List<WorkspaceResponse> responses = workspaces.stream()
                .map(this::convertToWorkspaceResponse)
                .collect(Collectors.toList());
        
        log.info("WorkspaceServiceImpl :: getWorkspacesByOwnerUserId :: Successfully retrieved {} workspaces for user :: {}", 
                responses.size(), ownerUserId);
        
        return responses;
    }


    private List<Workspace> createDefaultWorkspaces(UUID ownerUserId) {
        log.debug("WorkspaceServiceImpl :: createDefaultWorkspaces :: Creating default workspaces for user :: {}", ownerUserId);
        
        List<Workspace> defaultWorkspaces = new ArrayList<>();
        
        try {
            Workspace defaultWorkspace = Workspace.builder()
                    .name(DEFAULT_WORKSPACE_NAME)
                    .description(Constant.DEFAULT_WORKSPACE_DESC)
                    .workspaceType(WorkspaceType.DEFAULT)
                    .ownerUserId(ownerUserId)
                    .build();
            
            Workspace roadmapWorkspace = Workspace.builder()
                    .name(Constant.ROADMAP_WORKSPACE_NAME)
                    .description(Constant.ROADMAP_WORKSPACE_DESC)
                    .workspaceType(WorkspaceType.ROADMAP)
                    .ownerUserId(ownerUserId)
                    .build();
            
            Workspace projectWorkspace = Workspace.builder()
                    .name(Constant.PROJECT_WORKSPACE_NAME)
                    .description(Constant.PROJECT_WORKSPACE_DESC)
                    .workspaceType(WorkspaceType.GROUP_PROJECT)
                    .ownerUserId(ownerUserId)
                    .build();
            
            log.debug("WorkspaceServiceImpl :: createDefaultWorkspaces :: Saving default workspaces to database for user :: {}", ownerUserId);
            
            defaultWorkspaces.add(workspaceRepository.save(defaultWorkspace));
            log.debug("WorkspaceServiceImpl :: createDefaultWorkspaces :: Created workspace :: {} ({})", 
                    DEFAULT_WORKSPACE_NAME, defaultWorkspace.getId());
            
            defaultWorkspaces.add(workspaceRepository.save(roadmapWorkspace));
            log.debug("WorkspaceServiceImpl :: createDefaultWorkspaces :: Created workspace :: {} ({})", 
                    ROADMAP_WORKSPACE_NAME, roadmapWorkspace.getId());
            
            defaultWorkspaces.add(workspaceRepository.save(projectWorkspace));
            log.debug("WorkspaceServiceImpl :: createDefaultWorkspaces :: Created workspace :: {} ({})", 
                    PROJECT_WORKSPACE_NAME, projectWorkspace.getId());
            
            log.info("WorkspaceServiceImpl :: createDefaultWorkspaces :: Successfully created {} default workspaces for user :: {}", 
                    defaultWorkspaces.size(), ownerUserId);
            
        } catch (Exception e) {
            log.error("WorkspaceServiceImpl :: createDefaultWorkspaces :: Error creating default workspaces for user :: {} :: Error: {}", 
                    ownerUserId, e.getMessage(), e);
            throw new RuntimeException("Failed to create default workspaces for user: " + ownerUserId, e);
        }
        
        return defaultWorkspaces;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesAccessibleByUser(UUID userId) {
        log.info("WorkspaceServiceImpl :: getWorkspacesAccessibleByUser :: Started fetching accessible workspaces for user :: {}", userId);

        List<Workspace> userOwnedWorkspaces = workspaceRepository.findByOwnerUserId(userId);
        
        log.debug("WorkspaceServiceImpl :: getWorkspacesAccessibleByUser :: Found {} owned workspaces for user :: {}", 
                userOwnedWorkspaces.size(), userId);
        
        List<WorkspaceResponse> responses = userOwnedWorkspaces.stream()
                .distinct()
                .map(this::convertToWorkspaceResponse)
                .collect(Collectors.toList());
        
        log.info("WorkspaceServiceImpl :: getWorkspacesAccessibleByUser :: Successfully retrieved {} accessible workspaces for user :: {}", 
                responses.size(), userId);
        
        return responses;
    }

    @Override
    @Transactional
    public void deleteWorkspace(UUID id) {
        log.info("WorkspaceServiceImpl :: deleteWorkspace :: Started deleting workspace :: {}", id);

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("WorkspaceServiceImpl :: deleteWorkspace :: Workspace not found :: {}", id);
                    return new ResourceNotFoundException("Workspace not found with ID: " + id);
                });

        log.debug("WorkspaceServiceImpl :: deleteWorkspace :: Found workspace :: {} :: Name: {}", id, workspace.getName());
        workspaceRepository.delete(workspace);
        log.info("WorkspaceServiceImpl :: deleteWorkspace :: Workspace deleted successfully :: {}", id);
    }

    @Override
    @Transactional
    public void addUserToWorkspace(UUID workspaceId, UUID userId, WorkspaceAccess.AccessLevel accessLevel) {
        log.info("WorkspaceServiceImpl :: addUserToWorkspace :: Started adding user {} to workspace {} with access level {}", 
                userId, workspaceId, accessLevel);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> {
                    log.error("WorkspaceServiceImpl :: addUserToWorkspace :: Workspace not found :: {}", workspaceId);
                    return new ResourceNotFoundException("Workspace not found with ID: " + workspaceId);
                });

        // Check if user already has access
        log.debug("WorkspaceServiceImpl :: addUserToWorkspace :: Checking if user {} already has access to workspace {}", 
                userId, workspaceId);
        if (workspaceAccessRepository.findByWorkspaceIdAndUserId(workspaceId, userId).isPresent()) {
            log.warn("WorkspaceServiceImpl :: addUserToWorkspace :: User {} already has access to workspace {}", 
                    userId, workspaceId);
            throw new BadRequestException("User already has access to this workspace");
        }

        WorkspaceAccess access = WorkspaceAccess.builder()
                .workspace(workspace)
                .userId(userId)
                .accessLevel(accessLevel)
                .isActive(true)
                .build();

        workspaceAccessRepository.save(access);
        log.info("WorkspaceServiceImpl :: addUserToWorkspace :: Successfully added user {} to workspace {} with access level {}", 
                userId, workspaceId, accessLevel);
    }

    @Override
    @Transactional
    public void removeUserFromWorkspace(UUID workspaceId, UUID userId) {
        log.info("WorkspaceServiceImpl :: removeUserFromWorkspace :: Started removing user {} from workspace {}", 
                userId, workspaceId);

        if (!workspaceRepository.existsById(workspaceId)) {
            log.error("WorkspaceServiceImpl :: removeUserFromWorkspace :: Workspace not found :: {}", workspaceId);
            throw new ResourceNotFoundException("Workspace not found with ID: " + workspaceId);
        }

        log.debug("WorkspaceServiceImpl :: removeUserFromWorkspace :: Deleting access record for user {} in workspace {}", 
                userId, workspaceId);
        workspaceAccessRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId);
        log.info("WorkspaceServiceImpl :: removeUserFromWorkspace :: Successfully removed user {} from workspace {}", 
                userId, workspaceId);
    }

    @Override
    @Transactional
    public void updateUserAccessLevel(UUID workspaceId, UUID userId, WorkspaceAccess.AccessLevel accessLevel) {
        log.info("WorkspaceServiceImpl :: updateUserAccessLevel :: Started updating access level for user {} in workspace {} to {}", 
                userId, workspaceId, accessLevel);

        WorkspaceAccess access = workspaceAccessRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> {
                    log.error("WorkspaceServiceImpl :: updateUserAccessLevel :: User access not found :: User: {} :: Workspace: {}", 
                            userId, workspaceId);
                    return new ResourceNotFoundException("User access not found for workspace");
                });

        WorkspaceAccess.AccessLevel oldAccessLevel = access.getAccessLevel();
        access.setAccessLevel(accessLevel);
        workspaceAccessRepository.save(access);
        
        log.info("WorkspaceServiceImpl :: updateUserAccessLevel :: Successfully updated access level for user {} in workspace {} from {} to {}", 
                userId, workspaceId, oldAccessLevel, accessLevel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getWorkspaceUsers(UUID workspaceId) {
        log.info("WorkspaceServiceImpl :: getWorkspaceUsers :: Started fetching users for workspace :: {}", workspaceId);

        if (!workspaceRepository.existsById(workspaceId)) {
            log.error("WorkspaceServiceImpl :: getWorkspaceUsers :: Workspace not found :: {}", workspaceId);
            throw new ResourceNotFoundException("Workspace not found with ID: " + workspaceId);
        }

        List<UUID> userIds = workspaceAccessRepository.findByWorkspaceIdAndIsActiveTrue(workspaceId)
                .stream()
                .map(WorkspaceAccess::getUserId)
                .collect(Collectors.toList());
        
        log.info("WorkspaceServiceImpl :: getWorkspaceUsers :: Successfully retrieved {} users for workspace :: {}", 
                userIds.size(), workspaceId);
        
        return userIds;
    }

    @Override
    @Transactional
    public ApiResponse activateWorkspace(UUID userId) {
        log.info("WorkspaceServiceImpl :: activateWorkspace :: Activating default workspaces for user :: {}", userId);

        List<Workspace> existingWorkspaces = workspaceRepository.findByOwnerUserId(userId);

        if (!existingWorkspaces.isEmpty()) {
            log.warn("WorkspaceServiceImpl :: activateWorkspace :: User :: {} already has workspaces, activation skipped", userId);
            return ApiResponse.response("User already has workspaces", false);
        }

        createDefaultWorkspaces(userId);

        log.info("WorkspaceServiceImpl :: activateWorkspace :: Successfully activated default workspaces for user :: {}", userId);
        return ApiResponse.response("Default workspaces activated successfully", true);
    }

    @Override
    public Workspace getRoadmapWorkspaceByUserId(UUID userId) {
        log.info("WorkspaceServiceImpl :: getRoadmapWorkspaceByUserId :: Fetching roadmap workspace for user :: {}", userId);

        List<Workspace> workspaces = workspaceRepository.findByOwnerUserId(userId);
        if(workspaces.isEmpty()) {
            log.error("WorkspaceServiceImpl :: getRoadmapWorkspaceByUserId :: No workspaces found for user :: {}", userId);
            workspaces=createDefaultWorkspaces(userId);
        }


        for (Workspace workspace : workspaces) {
            if (workspace.getWorkspaceType() == WorkspaceType.ROADMAP) {
                log.info("WorkspaceServiceImpl :: getRoadmapWorkspaceByUserId :: Found roadmap workspace :: {} for user :: {}",
                        workspace.getId(), userId);
                return workspace;
            }
        }

        log.error("WorkspaceServiceImpl :: getRoadmapWorkspaceByUserId :: Roadmap workspace not found for user :: {}", userId);
        throw new ResourceNotFoundException("Roadmap workspace not found for user with ID: " + userId);
    }

    private WorkspaceResponse convertToWorkspaceResponse(Workspace workspace) {
        log.debug("WorkspaceServiceImpl :: convertToWorkspaceResponse :: Converting workspace :: {} to response", 
                workspace.getId());
        
        // Get access user IDs
        List<UUID> accessUserIds = workspaceAccessRepository.findByWorkspaceIdAndIsActiveTrue(workspace.getId())
                .stream()
                .map(WorkspaceAccess::getUserId)
                .collect(Collectors.toList());

        Long userAccessCount = workspaceAccessRepository.countActiveUsersByWorkspaceId(workspace.getId());

        log.debug("WorkspaceServiceImpl :: convertToWorkspaceResponse :: Workspace {} has {} active users with access", 
                workspace.getId(), userAccessCount);

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .ownerUserId(workspace.getOwnerUserId())
                .accessUserIds(accessUserIds)
                .userAccessCount(userAccessCount.intValue())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }
}