package com.SwitchBoard.WorkspaceService.service;

import com.SwitchBoard.WorkspaceService.dto.request.TaskUserAssignmentRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskAssignmentUpdateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.TaskAssignmentResponse;
import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import java.util.List;
import java.util.UUID;


public interface TaskAssignmentService {

    List<TaskAssignmentResponse> assignUsersToTask(UUID taskId, TaskUserAssignmentRequest request);


    void unassignUsersFromTask(UUID taskId, List<UUID> userIds);


    TaskAssignmentResponse updateTaskAssignment(UUID assignmentId, TaskAssignmentUpdateRequest request);


    List<TaskAssignmentResponse> getTaskAssignmentsByTaskId(UUID taskId);


    List<TaskAssignmentResponse> getTaskAssignmentsByUserId(UUID userId);

    TaskAssignmentResponse getTaskAssignmentByTaskAndUser(UUID taskId, UUID userId);

    List<TaskAssignmentResponse> getTaskAssignmentsByUserAndStatus(UUID userId, TaskStatus status);

    List<TaskAssignmentResponse> getOverdueTaskAssignments();

    void deleteAllTaskAssignments(UUID taskId);

    boolean isUserAlreadyAssigned(UUID taskId, UUID userId);
}