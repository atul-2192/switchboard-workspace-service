package com.SwitchBoard.WorkspaceService.controller;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentTaskManagementRequest;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentUpdateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.AssignmentResponse;
import com.SwitchBoard.WorkspaceService.dto.response.TaskResponse;
import com.SwitchBoard.WorkspaceService.service.impl.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Assignment Management", description = "APIs for managing assignments and their associated tasks in the learning management system")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    @Operation(
        summary = "Create a new assignment",
        description = "Creates a new assignment with optional tasks. You can either associate existing tasks or create new tasks along with the assignment. This endpoint supports both individual task management and bulk assignment creation with multiple tasks."
    )
    public ResponseEntity<ApiResponse> createAssignment(
            @Parameter(description = "Assignment creation request with optional tasks", required = true)
            @Valid @RequestBody AssignmentCreateRequest request,
            HttpServletRequest httpRequest) {
        log.info("AssignmentController :: createAssignment :: Creating assignment :: {}", request.getTitle());

        AssignmentResponse assignmentResponse = assignmentService.createAssignment(request);
        ApiResponse response = ApiResponse.response("Assignment created successfully", assignmentResponse, httpRequest.getRequestURI());

        log.info("AssignmentController :: createAssignment :: Assignment created :: {}", assignmentResponse.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get assignment by ID",
        description = "Retrieves a specific assignment by its unique identifier with complete details including associated tasks, statistics, and progress information. This endpoint provides comprehensive assignment data for detailed views."
    )
    public ResponseEntity<AssignmentResponse> getAssignmentById(
            @Parameter(description = "Unique UUID identifier of the assignment", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        log.info("AssignmentController :: getAssignmentById :: Fetching assignment :: {}", id);

        AssignmentResponse assignmentResponse = assignmentService.getAssignmentById(id);

        log.info("AssignmentController :: getAssignmentById :: Assignment retrieved :: {}", id);
        return ResponseEntity.ok(assignmentResponse);
    }

    @GetMapping
    @Operation(
        summary = "Get all assignments with pagination",
        description = "Retrieves a paginated list of all assignments in the system. This endpoint is useful for administrative overviews, assignment management dashboards, and generating system-wide reports. Results can be sorted by various criteria."
    )
    public ResponseEntity<Page<AssignmentResponse>> getAllAssignments(
            @Parameter(description = "Pagination and sorting parameters")
            Pageable pageable) {
        log.info("AssignmentController :: getAllAssignments :: Fetching assignments :: page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<AssignmentResponse> assignments = assignmentService.getAllAssignments(pageable);

        log.info("AssignmentController :: getAllAssignments :: Retrieved {} assignments", assignments.getTotalElements());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/workspace/{workspaceId}")
    @Operation(
        summary = "Get assignments by workspace",
        description = "Retrieves all assignments belonging to a specific workspace. This endpoint is essential for workspace-based assignment management, enabling users to view and manage assignments within their learning environment."
    )
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByWorkspaceId(
            @Parameter(description = "UUID of the workspace to get assignments for", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID workspaceId) {
        log.info("AssignmentController :: getAssignmentsByWorkspaceId :: Fetching assignments for workspace :: {}", workspaceId);

        List<AssignmentResponse> assignments = assignmentService.getAssignmentsByWorkspaceId(workspaceId);

        log.info("AssignmentController :: getAssignmentsByWorkspaceId :: Retrieved {} assignments for workspace {}", assignments.size(), workspaceId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/overdue")
    @Operation(
        summary = "Get overdue assignments",
        description = "Retrieves all assignments that are past their deadline. This endpoint is critical for identifying assignments that need immediate attention and helping administrators track delayed assignments."
    )
    public ResponseEntity<List<AssignmentResponse>> getOverdueAssignments() {
        log.info("AssignmentController :: getOverdueAssignments :: Fetching overdue assignments");

        List<AssignmentResponse> assignments = assignmentService.getOverdueAssignments();

        log.info("AssignmentController :: getOverdueAssignments :: Retrieved {} overdue assignments", assignments.size());
        return ResponseEntity.ok(assignments);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update assignment information",
        description = "Updates an existing assignment with new information such as title, description, deadline, and other assignment properties. This endpoint is used for assignment editing and maintaining up-to-date assignment information."
    )
    public ResponseEntity<ApiResponse> updateAssignment(
            @Parameter(description = "UUID of the assignment to update", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Parameter(description = "Assignment update request with modified fields", required = true)
            @Valid @RequestBody AssignmentUpdateRequest request,
            HttpServletRequest httpRequest) {
        log.info("AssignmentController :: updateAssignment :: Updating assignment :: {}", id);

        AssignmentResponse assignmentResponse = assignmentService.updateAssignment(id, request);
        ApiResponse response = ApiResponse.response("Assignment updated successfully", assignmentResponse, httpRequest.getRequestURI());

        log.info("AssignmentController :: updateAssignment :: Assignment updated :: {}", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete assignment",
        description = "Permanently removes an assignment from the system. This operation will also remove the assignment reference from all associated tasks, but the tasks themselves will remain in the system. Use with caution as this action cannot be undone."
    )
    public ResponseEntity<ApiResponse> deleteAssignment(
            @Parameter(description = "UUID of the assignment to delete", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        log.info("AssignmentController :: deleteAssignment :: Deleting assignment :: {}", id);

        assignmentService.deleteAssignment(id);
        ApiResponse response = ApiResponse.response("Assignment deleted successfully", null, httpRequest.getRequestURI());

        log.info("AssignmentController :: deleteAssignment :: Assignment deleted :: {}", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/tasks")
    @Operation(
        summary = "Get tasks by assignment",
        description = "Retrieves all tasks that belong to a specific assignment. This endpoint is essential for viewing assignment progress, managing task completion, and understanding the scope of work within an assignment."
    )
    public ResponseEntity<List<TaskResponse>> getTasksByAssignmentId(
            @Parameter(description = "UUID of the assignment to get tasks for", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        log.info("AssignmentController :: getTasksByAssignmentId :: Fetching tasks for assignment :: {}", id);

        List<TaskResponse> tasks = assignmentService.getTasksByAssignmentId(id);

        log.info("AssignmentController :: getTasksByAssignmentId :: Retrieved {} tasks for assignment {}", tasks.size(), id);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/{id}/tasks")
    @Operation(
        summary = "Add tasks to assignment",
        description = "Associates existing tasks with an assignment. This endpoint allows you to add multiple tasks to an assignment at once. All tasks must belong to the same workspace as the assignment."
    )
    public ResponseEntity<ApiResponse> addTasksToAssignment(
            @Parameter(description = "UUID of the assignment to add tasks to", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Parameter(description = "Request containing list of task IDs to add", required = true)
            @Valid @RequestBody TaskCreateRequest taskCreateRequest,
            HttpServletRequest httpRequest) {
        log.info("AssignmentController :: addTasksToAssignment :: Adding {} tasks to assignment :: {}", taskCreateRequest.getTasks().size(), id);

        AssignmentResponse assignmentResponse = assignmentService.addTasksToAssignment(id , taskCreateRequest);
        ApiResponse response = ApiResponse.response("Tasks added to assignment successfully", assignmentResponse, httpRequest.getRequestURI());

        log.info("AssignmentController :: addTasksToAssignment :: {} tasks added to assignment :: {}", taskCreateRequest.getTasks().size(), id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/tasks")
    @Operation(
        summary = "Remove tasks from assignment",
        description = "Removes the association between tasks and an assignment. The tasks remain in the system but are no longer part of the assignment. This is useful for reassigning tasks or restructuring assignments."
    )
    public ResponseEntity<ApiResponse> removeTasksFromAssignment(
            @Parameter(description = "UUID of the assignment to remove tasks from", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Parameter(description = "Request containing list of task IDs to remove", required = true)
            @Valid @RequestBody AssignmentTaskManagementRequest request,
            HttpServletRequest httpRequest) {
        log.info("AssignmentController :: removeTasksFromAssignment :: Removing {} tasks from assignment :: {}", request.getTaskIds().size(), id);

        AssignmentResponse assignmentResponse = assignmentService.removeTasksFromAssignment(id, request.getTaskIds());
        ApiResponse response = ApiResponse.response("Tasks removed from assignment successfully", assignmentResponse, httpRequest.getRequestURI());

        log.info("AssignmentController :: removeTasksFromAssignment :: {} tasks removed from assignment :: {}", request.getTaskIds().size(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/assign-users")
    @Operation(
        summary = "Assign users to all tasks in assignment",
        description = "Assigns multiple users to all tasks within an assignment, creating individual task instances for each user. This enables bulk user assignment at the assignment level for efficient task distribution."
    )
    public ResponseEntity<ApiResponse> assignUsersToAllTasks(
            @Parameter(description = "UUID of the assignment", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Parameter(description = "List of user IDs to assign", required = true)
            @RequestParam List<UUID> userIds,
            HttpServletRequest httpRequest) {
        log.info("AssignmentController :: assignUsersToAllTasks :: Assigning {} users to all tasks in assignment :: {}", userIds.size(), id);

        // Get the logged-in user ID from the header set by API Gateway
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        UUID assignedBy = userIdHeader != null ? UUID.fromString(userIdHeader) : null;

        AssignmentResponse assignmentResponse = assignmentService.assignUsersToAllTasks(id, userIds, assignedBy);
        ApiResponse response = ApiResponse.response("Users successfully assigned to all tasks in assignment", assignmentResponse, httpRequest.getRequestURI());

        log.info("AssignmentController :: assignUsersToAllTasks :: {} users assigned to all tasks in assignment :: {}", userIds.size(), id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/unassign-users")
    @Operation(
        summary = "Remove users from all tasks in assignment",
        description = "Removes user assignments from all tasks within an assignment, deleting their individual task instances and progress. This enables bulk user removal at the assignment level."
    )
    public ResponseEntity<ApiResponse> unassignUsersFromAllTasks(
            @Parameter(description = "UUID of the assignment", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Parameter(description = "List of user IDs to unassign", required = true)
            @RequestParam List<UUID> userIds,
            HttpServletRequest httpRequest) {
        log.info("AssignmentController :: unassignUsersFromAllTasks :: Unassigning {} users from all tasks in assignment :: {}", userIds.size(), id);

        assignmentService.unassignUsersFromAllTasks(id, userIds);
        ApiResponse response = ApiResponse.response("Users successfully unassigned from all tasks in assignment", null, httpRequest.getRequestURI());

        log.info("AssignmentController :: unassignUsersFromAllTasks :: {} users unassigned from all tasks in assignment :: {}", userIds.size(), id);
        return ResponseEntity.ok(response);
    }
}