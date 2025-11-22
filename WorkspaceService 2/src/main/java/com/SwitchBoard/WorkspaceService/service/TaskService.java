package com.SwitchBoard.WorkspaceService.service;

import com.SwitchBoard.WorkspaceService.dto.request.TaskCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.TaskResponse;
import com.SwitchBoard.WorkspaceService.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponse createTask(TaskCreateRequest request);
    
    TaskResponse getTaskById(UUID id);
    
    Page<TaskResponse> getAllTasks(Pageable pageable);
    
    List<TaskResponse> getTasksByWorkspaceId(UUID workspaceId);
    
    List<TaskResponse> getTasksByAssignmentId(UUID assignmentId);
    
    List<TaskResponse> getTasksByAssigneeId(UUID assigneeId);
    
    List<TaskResponse> getTasksByReporterId(UUID reporterId);
    
    List<TaskResponse> getSubTasks(UUID parentTaskId);
    
    List<TaskResponse> getRootTasksByWorkspaceId(UUID workspaceId);
    
    List<TaskResponse> getTasksByStatus(TaskStatus status);
    
    List<TaskResponse> getTasksByWorkspaceAndStatus(UUID workspaceId, TaskStatus status);
    
    List<TaskResponse> getOverdueTasks();
    
    List<TaskResponse> searchTasksByTitle(UUID workspaceId, String title);
    
    TaskResponse updateTask(UUID id, TaskCreateRequest request);
    
    TaskResponse updateTaskStatus(UUID id, TaskStatus status);
    
    TaskResponse assignTask(UUID id, UUID assigneeId);
    
    TaskResponse addTimeSpent(UUID id, Double hours);
    
    void deleteTask(UUID id);
    
    Long getSubTaskCount(UUID parentTaskId);
}