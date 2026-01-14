package com.SwitchBoard.WorkspaceService.controller;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.request.TaskCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.TaskResponse;
import com.SwitchBoard.WorkspaceService.entity.Task;
import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import com.SwitchBoard.WorkspaceService.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task Management", description = "APIs for managing tasks in the learning management system")
public class TaskController {

    private final TaskService taskService;

//    @PostMapping
//    @Operation(
//        summary = "Create a new task",
//        description = "Creates a new task in the system. Tasks are the core learning activities that can be assigned to users, organized in workspaces, and tracked for progress. A task can be part of an assignment, have subtasks, include due dates, and support time tracking for productivity analysis."
//    )
//    public ResponseEntity<ApiResponse> createTask(
//            @Parameter(description = "Task creation request with all task details", required = true)
//            @Valid @RequestBody TaskCreateRequest request,
//            HttpServletRequest httpRequest) {
//        log.info("TaskController :: createTask :: Creating new task");
//
//         List<Task> taskList = taskService.createTask(request);
//        ApiResponse response = ApiResponse.response( taskList.size() + " Task created successfully ",true);
//
//        log.info("TaskController :: createTask :: Task created successfully ");
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get task by ID",
        description = "Retrieves a specific task by its unique identifier. This endpoint provides complete task information including title, description, status, assignment details, time tracking, and relationships with other tasks. Used for task detail views and editing."
    )
    public ResponseEntity<TaskResponse> getTaskById(
            @Parameter(description = "Unique UUID identifier of the task", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id) {
        log.info("TaskController :: getTaskById :: Fetching task :: {}", id);
        
        TaskResponse taskResponse = taskService.getTaskById(id);
        
        log.info("TaskController :: getTaskById :: Task retrieved successfully :: {}", id);
        return ResponseEntity.ok(taskResponse);
    }

    @GetMapping
    @Operation(
        summary = "Get all tasks with pagination",
        description = "Retrieves a paginated list of all tasks in the system. This endpoint is useful for administrative overviews, global task management, and generating system-wide reports. Results can be sorted by various criteria including creation date, due date, and priority."
    )
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            @Parameter(description = "Pagination and sorting parameters")
            Pageable pageable) {
        log.info("TaskController :: getAllTasks :: Fetching all tasks :: page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<TaskResponse> tasks = taskService.getAllTasks(pageable);
        
        log.info("TaskController :: getAllTasks :: Retrieved {} tasks", tasks.getTotalElements());
        return ResponseEntity.ok(tasks);
    }

//    @GetMapping("/workspace/{workspaceId}")
//    @Operation(
//        summary = "Get tasks by workspace",
//        description = "Retrieves all tasks belonging to a specific workspace. This is a core endpoint for workspace-based task management, enabling users to view and manage tasks within their learning environment. Tasks are displayed with their current status and assignment information."
//    )
//    public ResponseEntity<List<TaskResponse>> getTasksByWorkspaceId(
//            @Parameter(description = "UUID of the workspace to get tasks for", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
//            @PathVariable UUID workspaceId) {
//        log.info("TaskController :: getTasksByWorkspaceId :: Fetching tasks for workspace :: {}", workspaceId);
//
//        List<TaskResponse> tasks = taskService.getTasksByWorkspaceId(workspaceId);
//
//        log.info("TaskController :: getTasksByWorkspaceId :: Retrieved {} tasks for workspace {}", tasks.size(), workspaceId);
//        return ResponseEntity.ok(tasks);
//    }

    @GetMapping("/assignment/{assignmentId}")
    @Operation(
        summary = "Get tasks by assignment",
        description = "Retrieves all tasks that belong to a specific assignment. This endpoint is essential for assignment-based learning where tasks are grouped together as part of a larger learning objective or project. Useful for tracking assignment completion and progress."
    )
    public ResponseEntity<List<TaskResponse>> getTasksByAssignmentId(
            @Parameter(description = "UUID of the assignment to get tasks for", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID assignmentId) {
        log.info("TaskController :: getTasksByAssignmentId :: Fetching tasks for assignment :: {}", assignmentId);
        
        List<TaskResponse> tasks = taskService.getTasksByAssignmentId(assignmentId);
        
        log.info("TaskController :: getTasksByAssignmentId :: Retrieved {} tasks for assignment {}", tasks.size(), assignmentId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assigned-to-me")
    @Operation(
        summary = "Get tasks assigned to authenticated user",
        description = "Retrieves all tasks assigned to the authenticated user. This endpoint is crucial for personal task management, allowing users to view their workload, track progress, and prioritize their learning activities. Commonly used in personal dashboards and to-do lists."
    )
    public ResponseEntity<List<TaskResponse>> getTasksByAssigneeId(
            HttpServletRequest httpRequest) {
        // Get the logged-in user ID from the header set by API Gateway
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            throw new IllegalArgumentException("User ID not found in request header");
        }
        UUID assigneeId = UUID.fromString(userIdHeader);
        
        log.info("TaskController :: getTasksByAssigneeId :: Fetching tasks for assignee :: {}", assigneeId);
        
        List<TaskResponse> tasks = taskService.getTasksByAssigneeId(assigneeId);
        
        log.info("TaskController :: getTasksByAssigneeId :: Retrieved {} tasks for assignee {}", tasks.size(), assigneeId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/created-by-me")
    @Operation(
        summary = "Get tasks created by authenticated user",
        description = "Retrieves all tasks that were created/reported by the authenticated user. This endpoint helps track task creation patterns, manage instructor-created assignments, and provide accountability for task generation within the learning system."
    )
    public ResponseEntity<List<TaskResponse>> getTasksByReporterId(
            HttpServletRequest httpRequest) {
        // Get the logged-in user ID from the header set by API Gateway
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            throw new IllegalArgumentException("User ID not found in request header");
        }
        UUID reporterId = UUID.fromString(userIdHeader);
        
        log.info("TaskController :: getTasksByReporterId :: Fetching tasks for reporter :: {}", reporterId);
        
        List<TaskResponse> tasks = taskService.getTasksByReporterId(reporterId);
        
        log.info("TaskController :: getTasksByReporterId :: Retrieved {} tasks for reporter {}", tasks.size(), reporterId);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update task",
        description = "Updates an existing task with new information such as title, description, due date, priority, and other task properties. This endpoint is used for task editing, maintaining up-to-date task information, and adapting tasks as requirements change."
    )
    public ResponseEntity<ApiResponse> updateTask(
            @Parameter(description = "UUID of the task to update", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id, 
            @Parameter(description = "Task update request with modified fields", required = true)
            @Valid @RequestBody TaskCreateRequest request,
            HttpServletRequest httpRequest) {
        log.info("TaskController :: updateTask :: Updating task :: {}", id);
        
        TaskResponse taskResponse = taskService.updateTask(id, request);
        ApiResponse response = ApiResponse.response("Task updated successfully", taskResponse, httpRequest.getRequestURI());
        
        log.info("TaskController :: updateTask :: Task updated successfully :: {}", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Update task status",
        description = "Updates the status of a specific task. This endpoint is crucial for workflow management, progress tracking, and moving tasks through different stages of completion. Status changes may trigger notifications and update related metrics."
    )
    public ResponseEntity<ApiResponse> updateTaskStatus(
            @Parameter(description = "UUID of the task to update status for", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id, 
            @Parameter(description = "New status for the task", required = true, 
                      schema = @Schema(type = "string", allowableValues = {"TODO", "IN_PROGRESS", "DONE", "CANCELLED"}),
                      example = "DONE")
            @RequestParam TaskStatus status,
            HttpServletRequest httpRequest) {
        log.info("TaskController :: updateTaskStatus :: Updating task status :: id: {}, status: {}", id, status);
        
        TaskResponse taskResponse = taskService.updateTaskStatus(id, status);
        ApiResponse response = ApiResponse.response("Task status updated successfully", taskResponse, httpRequest.getRequestURI());
        
        log.info("TaskController :: updateTaskStatus :: Task status updated successfully :: {}", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/assign")
    @Operation(
        summary = "Assign task to user",
        description = "Assigns a task to a specific user, making them responsible for its completion. This endpoint is essential for task distribution, workload management, and establishing accountability in team-based learning environments."
    )
    public ResponseEntity<ApiResponse> assignTask(
            @Parameter(description = "UUID of the task to assign", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id, 
            @Parameter(description = "UUID of the user to assign the task to", required = true, example = "550e8400-e29b-41d4-a716-446655440001")
            @RequestParam UUID assigneeId,
            HttpServletRequest httpRequest) {
        log.info("TaskController :: assignTask :: Assigning task :: id: {}, assignee: {}", id, assigneeId);
        
        TaskResponse taskResponse = taskService.assignTask(id, assigneeId);
        ApiResponse response = ApiResponse.response("Task assigned successfully", taskResponse, httpRequest.getRequestURI());
        
        log.info("TaskController :: assignTask :: Task assigned successfully :: {}", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete task",
        description = "Permanently removes a task from the system. This is a soft delete operation that maintains data integrity for historical records while preventing the task from appearing in active task lists. Use with caution as this action affects task hierarchies and assignments."
    )
    public ResponseEntity<ApiResponse> deleteTask(
            @Parameter(description = "UUID of the task to delete", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id, 
            HttpServletRequest httpRequest) {
        log.info("TaskController :: deleteTask :: Deleting task :: {}", id);
        
        taskService.deleteTask(id);
        ApiResponse response = ApiResponse.response("Task deleted successfully", null, httpRequest.getRequestURI());
        
        log.info("TaskController :: deleteTask :: Task deleted successfully :: {}", id);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/{parentTaskId}/subtasks/count")
//    @Operation(
//        summary = "Get subtask count",
//        description = "Returns the number of subtasks belonging to a specific parent task. This endpoint is useful for displaying task complexity, progress indicators, and understanding the scope of hierarchical tasks without loading all subtask details."
//    )
//    public ResponseEntity<Long> getSubTaskCount(
//            @Parameter(description = "UUID of the parent task to count subtasks for", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
//            @PathVariable UUID parentTaskId) {
//        log.info("TaskController :: getSubTaskCount :: Counting subtasks :: {}", parentTaskId);
//
//        Long count = taskService.getSubTaskCount(parentTaskId);
//
//        log.info("TaskController :: getSubTaskCount :: Task {} has {} subtasks", parentTaskId, count);
//        return ResponseEntity.ok(count);
//    }
}