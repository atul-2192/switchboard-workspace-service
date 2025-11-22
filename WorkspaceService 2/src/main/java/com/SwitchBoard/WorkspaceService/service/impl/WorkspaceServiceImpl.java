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

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest request) {
        log.info("WorkspaceServiceImpl :: createWorkspace :: Creating workspace :: {}", request.getName());

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .visibility(request.getVisibility())
                .ownerUserId(request.getOwnerUserId())
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        log.info("WorkspaceServiceImpl :: createWorkspace :: Workspace created :: {}", savedWorkspace.getId());

        // Validate that no user appears in multiple access lists
        List<String> validationErrors = request.validateUserAccess();
        if (!validationErrors.isEmpty()) {
            throw new BadRequestException("User access validation failed: " + String.join(", ", validationErrors));
        }

        // Create workspace access records for specified users
        createUserAccessRecords(savedWorkspace, request);

        return convertToWorkspaceResponse(savedWorkspace);
    }

    /**
     * Creates user access records for the workspace based on the three access lists
     */
    private void createUserAccessRecords(Workspace workspace, WorkspaceCreateRequest request) {
        List<WorkspaceAccess> accessRecords = new ArrayList<>();

        // Create READ access records
        if (request.getReadAccessUserIds() != null) {
            for (UUID userId : request.getReadAccessUserIds()) {
                if (!userId.equals(request.getOwnerUserId())) { // Don't add owner as they have implicit access
                    accessRecords.add(WorkspaceAccess.builder()
                            .workspace(workspace)
                            .userId(userId)
                            .accessLevel(WorkspaceAccess.AccessLevel.read)
                            .isActive(true)
                            .build());
                }
            }
        }

        // Create WRITE access records
        if (request.getWriteAccessUserIds() != null) {
            for (UUID userId : request.getWriteAccessUserIds()) {
                if (!userId.equals(request.getOwnerUserId())) {
                    accessRecords.add(WorkspaceAccess.builder()
                            .workspace(workspace)
                            .userId(userId)
                            .accessLevel(WorkspaceAccess.AccessLevel.WRITE)
                            .isActive(true)
                            .build());
                }
            }
        }

        // Create ADMIN access records
        if (request.getAdminAccessUserIds() != null) {
            for (UUID userId : request.getAdminAccessUserIds()) {
                if (!userId.equals(request.getOwnerUserId())) {
                    accessRecords.add(WorkspaceAccess.builder()
                            .workspace(workspace)
                            .userId(userId)
                            .accessLevel(WorkspaceAccess.AccessLevel.ADMIN)
                            .isActive(true)
                            .build());
                }
            }
        }

        if (!accessRecords.isEmpty()) {
            workspaceAccessRepository.saveAll(accessRecords);
            log.info("WorkspaceServiceImpl :: createUserAccessRecords :: Added access for {} users", accessRecords.size());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID id) {
        log.info("WorkspaceServiceImpl :: getWorkspaceById :: Fetching workspace :: {}", id);

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + id));

        return convertToWorkspaceResponse(workspace);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesByOwnerUserId(UUID ownerUserId) {
        log.info("WorkspaceServiceImpl :: getWorkspacesByOwnerUserId :: Fetching workspaces for user :: {}", ownerUserId);

        List<Workspace> workspaces = workspaceRepository.findByOwnerUserId(ownerUserId);
        return workspaces.stream()
                .map(this::convertToWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesAccessibleByUser(UUID userId) {
        log.info("WorkspaceServiceImpl :: getWorkspacesAccessibleByUser :: Fetching accessible workspaces for user :: {}", userId);

        // This would typically involve complex logic checking user permissions, group memberships, etc.
        // For now, returning workspaces owned by user + public workspaces
        List<Workspace> userOwnedWorkspaces = workspaceRepository.findByOwnerUserId(userId);
        List<Workspace> publicWorkspaces = workspaceRepository.findByVisibility(Workspace.WorkspaceVisibility.PUBLIC);

        userOwnedWorkspaces.addAll(publicWorkspaces);
        return userOwnedWorkspaces.stream()
                .distinct()
                .map(this::convertToWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getWorkspacesByVisibility(Workspace.WorkspaceVisibility visibility) {
        log.info("WorkspaceServiceImpl :: getWorkspacesByVisibility :: Fetching workspaces with visibility :: {}", visibility);

        List<Workspace> workspaces = workspaceRepository.findByVisibility(visibility);
        return workspaces.stream()
                .map(this::convertToWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> searchWorkspacesByName(String name) {
        log.info("WorkspaceServiceImpl :: searchWorkspacesByName :: Searching workspaces with name :: {}", name);

        List<Workspace> workspaces = workspaceRepository.findByNameContainingIgnoreCase(name);
        return workspaces.stream()
                .map(this::convertToWorkspaceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(UUID id, WorkspaceCreateRequest request) {
        log.info("WorkspaceServiceImpl :: updateWorkspace :: Updating workspace :: {}", id);

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + id));

        // Update fields
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace.setVisibility(request.getVisibility());
        workspace.setOwnerUserId(request.getOwnerUserId());

        Workspace updatedWorkspace = workspaceRepository.save(workspace);

        // Validate that no user appears in multiple access lists
        List<String> validationErrors = request.validateUserAccess();
        if (!validationErrors.isEmpty()) {
            throw new BadRequestException("User access validation failed: " + String.join(", ", validationErrors));
        }

        // Update workspace access
        // Remove existing access records
        workspaceAccessRepository.deleteByWorkspaceId(id);
        
        // Add new access records based on the three lists
        createUserAccessRecords(updatedWorkspace, request);

        log.info("WorkspaceServiceImpl :: updateWorkspace :: Workspace updated :: {}", updatedWorkspace.getId());

        return convertToWorkspaceResponse(updatedWorkspace);
    }

    @Override
    @Transactional
    public void deleteWorkspace(UUID id) {
        log.info("WorkspaceServiceImpl :: deleteWorkspace :: Deleting workspace :: {}", id);

        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + id));

        workspaceRepository.delete(workspace);
        log.info("WorkspaceServiceImpl :: deleteWorkspace :: Workspace deleted :: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTaskCount(UUID workspaceId) {
        log.info("WorkspaceServiceImpl :: getTaskCount :: Getting task count for workspace :: {}", workspaceId);

        if (!workspaceRepository.existsById(workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with ID: " + workspaceId);
        }

        return workspaceRepository.countTasksByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getAssignmentCount(UUID workspaceId) {
        log.info("WorkspaceServiceImpl :: getAssignmentCount :: Getting assignment count for workspace :: {}", workspaceId);

        if (!workspaceRepository.existsById(workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with ID: " + workspaceId);
        }

        return workspaceRepository.countAssignmentsByWorkspaceId(workspaceId);
    }

    @Override
    @Transactional
    public void addUserToWorkspace(UUID workspaceId, UUID userId, WorkspaceAccess.AccessLevel accessLevel) {
        log.info("WorkspaceServiceImpl :: addUserToWorkspace :: Adding user {} to workspace {}", userId, workspaceId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));

        // Check if user already has access
        if (workspaceAccessRepository.findByWorkspaceIdAndUserId(workspaceId, userId).isPresent()) {
            throw new BadRequestException("User already has access to this workspace");
        }

        WorkspaceAccess access = WorkspaceAccess.builder()
                .workspace(workspace)
                .userId(userId)
                .accessLevel(accessLevel)
                .isActive(true)
                .build();

        workspaceAccessRepository.save(access);
        log.info("WorkspaceServiceImpl :: addUserToWorkspace :: User {} added to workspace {}", userId, workspaceId);
    }

    @Override
    @Transactional
    public void removeUserFromWorkspace(UUID workspaceId, UUID userId) {
        log.info("WorkspaceServiceImpl :: removeUserFromWorkspace :: Removing user {} from workspace {}", userId, workspaceId);

        if (!workspaceRepository.existsById(workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with ID: " + workspaceId);
        }

        workspaceAccessRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId);
        log.info("WorkspaceServiceImpl :: removeUserFromWorkspace :: User {} removed from workspace {}", userId, workspaceId);
    }

    @Override
    @Transactional
    public void updateUserAccessLevel(UUID workspaceId, UUID userId, WorkspaceAccess.AccessLevel accessLevel) {
        log.info("WorkspaceServiceImpl :: updateUserAccessLevel :: Updating access level for user {} in workspace {}", userId, workspaceId);

        WorkspaceAccess access = workspaceAccessRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User access not found for workspace"));

        access.setAccessLevel(accessLevel);
        workspaceAccessRepository.save(access);
        log.info("WorkspaceServiceImpl :: updateUserAccessLevel :: Access level updated for user {} in workspace {}", userId, workspaceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getWorkspaceUsers(UUID workspaceId) {
        log.info("WorkspaceServiceImpl :: getWorkspaceUsers :: Getting users for workspace {}", workspaceId);

        if (!workspaceRepository.existsById(workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with ID: " + workspaceId);
        }

        return workspaceAccessRepository.findByWorkspaceIdAndIsActiveTrue(workspaceId)
                .stream()
                .map(WorkspaceAccess::getUserId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserAccess(UUID workspaceId, UUID userId) {
        log.info("WorkspaceServiceImpl :: hasUserAccess :: Checking access for user {} in workspace {}", userId, workspaceId);

        if (!workspaceRepository.existsById(workspaceId)) {
            throw new ResourceNotFoundException("Workspace not found with ID: " + workspaceId);
        }

        // Check if user is owner or has explicit access
        Workspace workspace = workspaceRepository.findById(workspaceId).get();
        return workspace.getOwnerUserId().equals(userId) || 
               workspaceAccessRepository.hasUserAccess(workspaceId, userId);
    }

    private WorkspaceResponse convertToWorkspaceResponse(Workspace workspace) {
        // Get access user IDs
        List<UUID> accessUserIds = workspaceAccessRepository.findByWorkspaceIdAndIsActiveTrue(workspace.getId())
                .stream()
                .map(WorkspaceAccess::getUserId)
                .collect(Collectors.toList());

        Long userAccessCount = workspaceAccessRepository.countActiveUsersByWorkspaceId(workspace.getId());

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .visibility(workspace.getVisibility())
                .ownerUserId(workspace.getOwnerUserId())
                .accessUserIds(accessUserIds)
                .userAccessCount(userAccessCount.intValue())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }
}