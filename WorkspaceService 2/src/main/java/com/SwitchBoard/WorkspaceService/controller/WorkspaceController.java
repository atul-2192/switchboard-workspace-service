package com.SwitchBoard.WorkspaceService.controller;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.request.WorkspaceCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.WorkspaceResponse;
import com.SwitchBoard.WorkspaceService.entity.Workspace;
import com.SwitchBoard.WorkspaceService.entity.WorkspaceAccess;
import com.SwitchBoard.WorkspaceService.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workspace Management", description = "APIs for managing workspaces - organizational containers for assignments, tasks, and learning activities")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @Operation(
        summary = "Create a new workspace",
        description = "Creates a new workspace that serves as an organizational container for assignments, tasks, and learning activities. Workspaces enable multi-tenancy, access control, and logical separation of different learning environments or projects."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Workspace created successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse> createWorkspace(
            @Parameter(description = "Workspace creation request with workspace details", required = true)
            @Valid @RequestBody WorkspaceCreateRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        log.info("WorkspaceController :: createWorkspace :: Creating workspace :: {}", request.getName());
        UUID userId = UUID.fromString(userIdHeader);
        request.setOwnerUserId(userId);

        WorkspaceResponse workspaceResponse = workspaceService.createWorkspace(request);
        ApiResponse response = ApiResponse.success("Workspace created successfully ", workspaceResponse, "/api/v1/workspaces/" + workspaceResponse.getId());

        log.info("WorkspaceController :: createWorkspace :: Workspace created :: {}", workspaceResponse.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get workspace by ID",
        description = "Retrieves a specific workspace by its unique identifier, including its metadata and statistics."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workspace retrieved successfully",
            content = @Content(schema = @Schema(implementation = WorkspaceResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workspace not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<WorkspaceResponse> getWorkspaceById(
            @Parameter(description = "UUID of the workspace", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        log.info("WorkspaceController :: getWorkspaceById :: Fetching workspace :: {}", id);

        WorkspaceResponse workspaceResponse = workspaceService.getWorkspaceById(id);

        return ResponseEntity.ok(workspaceResponse);
    }

    @GetMapping("/owner")
    @Operation(
        summary = "Get workspaces by owner user",
        description = "Retrieves all workspaces owned by the authenticated user. Users can own multiple workspaces for different projects or learning contexts."
    )
    public ResponseEntity<List<WorkspaceResponse>> getWorkspacesByOwnerUserId(
            @RequestHeader("X-User-Id") String userIdHeader){
        // Get the logged-in user ID from the header set by API Gateway
        UUID userId = UUID.fromString(userIdHeader);
        
        log.info("WorkspaceController :: getWorkspacesByOwnerUserId :: Fetching workspaces for user :: {}", userId);

        List<WorkspaceResponse> workspaces = workspaceService.getWorkspacesByOwnerUserId(userId);

        return ResponseEntity.ok(workspaces);
    }

    @GetMapping("/accessible")
    @Operation(
        summary = "Get workspaces accessible by user",
        description = "Retrieves all workspaces that the authenticated user has access to, including owned workspaces and workspaces shared with them through explicit user access permissions."
    )
    public ResponseEntity<List<WorkspaceResponse>> getWorkspacesAccessibleByUser(
            HttpServletRequest httpRequest) {
        // Get the logged-in user ID from the header set by API Gateway
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            throw new IllegalArgumentException("User ID not found in request header");
        }
        UUID userId = UUID.fromString(userIdHeader);
        
        log.info("WorkspaceController :: getWorkspacesAccessibleByUser :: Fetching accessible workspaces for user :: {}", userId);

        List<WorkspaceResponse> workspaces = workspaceService.getWorkspacesAccessibleByUser(userId);

        return ResponseEntity.ok(workspaces);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update workspace",
        description = "Updates an existing workspace's details including name, description, visibility, and ownership."
    )
    public ResponseEntity<ApiResponse> updateWorkspace(
            @Parameter(description = "UUID of the workspace to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody WorkspaceCreateRequest request,
            HttpServletRequest httpRequest) {
        log.info("WorkspaceController :: updateWorkspace :: Updating workspace :: {}", id);

        WorkspaceResponse workspaceResponse = workspaceService.updateWorkspace(id, request);
        ApiResponse response = ApiResponse.success("Workspace updated successfully", workspaceResponse, httpRequest.getRequestURI());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete workspace",
        description = "Deletes a workspace permanently along with all its assignments, tasks, and related data. This action cannot be undone."
    )
    public ResponseEntity<ApiResponse> deleteWorkspace(
            @Parameter(description = "UUID of the workspace to delete", required = true)
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        log.info("WorkspaceController :: deleteWorkspace :: Deleting workspace :: {}", id);

        workspaceService.deleteWorkspace(id);
        ApiResponse response = ApiResponse.success("Workspace deleted successfully", null, httpRequest.getRequestURI());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{workspaceId}/users/{userId}")
    @Operation(
        summary = "Add user to workspace",
        description = "Grants a user access to a workspace with specified access level."
    )
    public ResponseEntity<ApiResponse> addUserToWorkspace(
            @Parameter(description = "UUID of the workspace", required = true)
            @PathVariable UUID workspaceId,
            @Parameter(description = "UUID of the user to add", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Access level to grant", required = true)
            @RequestParam WorkspaceAccess.AccessLevel accessLevel,
            HttpServletRequest httpRequest) {
        log.info("WorkspaceController :: addUserToWorkspace :: Adding user {} to workspace {} with access level {}", 
                userId, workspaceId, accessLevel);

        workspaceService.addUserToWorkspace(workspaceId, userId, accessLevel);
        ApiResponse response = ApiResponse.success("User added to workspace successfully", null, httpRequest.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{workspaceId}/users/{userId}")
    @Operation(
        summary = "Remove user from workspace",
        description = "Removes a user's access to a workspace."
    )
    public ResponseEntity<ApiResponse> removeUserFromWorkspace(
            @Parameter(description = "UUID of the workspace", required = true)
            @PathVariable UUID workspaceId,
            @Parameter(description = "UUID of the user to remove", required = true)
            @PathVariable UUID userId,
            HttpServletRequest httpRequest) {
        log.info("WorkspaceController :: removeUserFromWorkspace :: Removing user {} from workspace {}", userId, workspaceId);

        workspaceService.removeUserFromWorkspace(workspaceId, userId);
        ApiResponse response = ApiResponse.success("User removed from workspace successfully", null, httpRequest.getRequestURI());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{workspaceId}/users/{userId}/access")
    @Operation(
        summary = "Update user access level",
        description = "Updates a user's access level for a workspace."
    )
    public ResponseEntity<ApiResponse> updateUserAccessLevel(
            @Parameter(description = "UUID of the workspace", required = true)
            @PathVariable UUID workspaceId,
            @Parameter(description = "UUID of the user", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "New access level", required = true)
            @RequestParam WorkspaceAccess.AccessLevel accessLevel,
            HttpServletRequest httpRequest) {
        log.info("WorkspaceController :: updateUserAccessLevel :: Updating access level for user {} in workspace {} to {}", 
                userId, workspaceId, accessLevel);

        workspaceService.updateUserAccessLevel(workspaceId, userId, accessLevel);
        ApiResponse response = ApiResponse.success("User access level updated successfully", null, httpRequest.getRequestURI());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{workspaceId}/users")
    @Operation(
        summary = "Get workspace users",
        description = "Retrieves all users who have access to a workspace."
    )
    public ResponseEntity<List<UUID>> getWorkspaceUsers(
            @Parameter(description = "UUID of the workspace", required = true)
            @PathVariable UUID workspaceId) {
        log.info("WorkspaceController :: getWorkspaceUsers :: Getting users for workspace {}", workspaceId);

        List<UUID> userIds = workspaceService.getWorkspaceUsers(workspaceId);

        return ResponseEntity.ok(userIds);
    }
}