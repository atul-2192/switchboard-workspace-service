package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.dto.request.WorkspaceCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.WorkspaceResponse;
import com.SwitchBoard.WorkspaceService.entity.Workspace;
import com.SwitchBoard.WorkspaceService.entity.WorkspaceAccess;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceAccessRepository workspaceAccessRepository;

    // Constants for default workspace configurations
    private static final String DEFAULT_WORKSPACE_NAME = "Default Workspace";
    private static final String DEFAULT_WORKSPACE_DESC = "Your personal space to organize tasks, ideas, and notes. Only you have access to this workspace.";
    private static final String ROADMAP_WORKSPACE_NAME = "Roadmap Workspace";
    private static final String ROADMAP_WORKSPACE_DESC = "A dedicated workspace to manage roadmaps, milestones, and learning or project journeys.";
    private static final String PROJECT_WORKSPACE_NAME = "Project Workspace";
    private static final String PROJECT_WORKSPACE_DESC = "A collaborative workspace for teams to work together on tasks, discussions, and shared goals.";

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest request) {
        log.info("WorkspaceServiceImpl :: createWorkspace :: Started creating workspace :: {} for owner :: {}", 
                request.getName(), request.getOwnerUserId());
        
        log.debug("WorkspaceServiceImpl :: createWorkspace :: Request details :: Name: {}, Description: {}", 
                request.getName(), request.getDescription());

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ownerUserId(request.getOwnerUserId())
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        log.info("WorkspaceServiceImpl :: createWorkspace :: Workspace created successfully :: ID: {}, Name: {}", 
                savedWorkspace.getId(), savedWorkspace.getName());

        // Validate that no user appears in multiple access lists
        log.debug("WorkspaceServiceImpl :: createWorkspace :: Validating user access lists");
        List<String> validationErrors = request.validateUserAccess();
        if (!validationErrors.isEmpty()) {
            log.error("WorkspaceServiceImpl :: createWorkspace :: User access validation failed :: Errors: {}", validationErrors);
            throw new BadRequestException("User access validation failed: " + String.join(", ", validationErrors));
        }

        // Create workspace access records for specified users
        log.debug("WorkspaceServiceImpl :: createWorkspace :: Creating user access records for workspace :: {}", savedWorkspace.getId());
        createUserAccessRecords(savedWorkspace, request);

        log.info("WorkspaceServiceImpl :: createWorkspace :: Workspace creation completed successfully :: {}", savedWorkspace.getId());
        return convertToWorkspaceResponse(savedWorkspace);
    }

    /**
     * Creates user access records for the workspace based on the three access lists
     */
    private void createUserAccessRecords(Workspace workspace, WorkspaceCreateRequest request) {
        log.debug("WorkspaceServiceImpl :: createUserAccessRecords :: Creating access records for workspace :: {}", workspace.getId());
        
        List<WorkspaceAccess> accessRecords = new ArrayList<>();
        int readAccessCount = 0;
        int writeAccessCount = 0;
        int adminAccessCount = 0;

        // Create READ access records
        if (request.getReadAccessUserIds() != null) {
            log.debug("WorkspaceServiceImpl :: createUserAccessRecords :: Processing {} read access users", 
                    request.getReadAccessUserIds().size());
            for (UUID userId : request.getReadAccessUserIds()) {
                if (!userId.equals(request.getOwnerUserId())) {
                    accessRecords.add(WorkspaceAccess.builder()
                            .workspace(workspace)
                            .userId(userId)
                            .accessLevel(WorkspaceAccess.AccessLevel.read)
                            .isActive(true)
                            .build());
                    readAccessCount++;
                }
            }
        }

        // Create WRITE access records
        if (request.getWriteAccessUserIds() != null) {
            log.debug("WorkspaceServiceImpl :: createUserAccessRecords :: Processing {} write access users", 
                    request.getWriteAccessUserIds().size());
            for (UUID userId : request.getWriteAccessUserIds()) {
                if (!userId.equals(request.getOwnerUserId())) {
                    accessRecords.add(WorkspaceAccess.builder()
                            .workspace(workspace)
                            .userId(userId)
                            .accessLevel(WorkspaceAccess.AccessLevel.WRITE)
                            .isActive(true)
                            .build());
                    writeAccessCount++;
                }
            }
        }

        // Create ADMIN access records
        if (request.getAdminAccessUserIds() != null) {
            log.debug("WorkspaceServiceImpl :: createUserAccessRecords :: Processing {} admin access users", 
                    request.getAdminAccessUserIds().size());
            for (UUID userId : request.getAdminAccessUserIds()) {
                if (!userId.equals(request.getOwnerUserId())) {
                    accessRecords.add(WorkspaceAccess.builder()
                            .workspace(workspace)
                            .userId(userId)
                            .accessLevel(WorkspaceAccess.AccessLevel.ADMIN)
                            .isActive(true)
                            .build());
                    adminAccessCount++;
                }
            }
        }

        if (!accessRecords.isEmpty()) {
            workspaceAccessRepository.saveAll(accessRecords);
            log.info("WorkspaceServiceImpl :: createUserAccessRecords :: Access records created successfully :: Workspace: {} :: Total: {} (READ: {}, WRITE: {}, ADMIN: {})", 
                    workspace.getId(), accessRecords.size(), readAccessCount, writeAccessCount, adminAccessCount);
        } else {
            log.debug("WorkspaceServiceImpl :: createUserAccessRecords :: No access records to create for workspace :: {}", workspace.getId());
        }
    }

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
                    .description(DEFAULT_WORKSPACE_DESC)
                    .workspaceType(Workspace.WorkspaceType.DEFAULT)
                    .ownerUserId(ownerUserId)
                    .build();
            
            Workspace roadmapWorkspace = Workspace.builder()
                    .name(ROADMAP_WORKSPACE_NAME)
                    .description(ROADMAP_WORKSPACE_DESC)
                    .workspaceType(Workspace.WorkspaceType.ROADMAP)
                    .ownerUserId(ownerUserId)
                    .build();
            
            Workspace projectWorkspace = Workspace.builder()
                    .name(PROJECT_WORKSPACE_NAME)
                    .description(PROJECT_WORKSPACE_DESC)
                    .workspaceType(Workspace.WorkspaceType.GROUP_PROJECT)
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