package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.dto.request.TaskUserAssignmentRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskAssignmentUpdateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.TaskAssignmentResponse;
import com.SwitchBoard.WorkspaceService.entity.*;
import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import com.SwitchBoard.WorkspaceService.repository.TaskAssignmentRepository;
import com.SwitchBoard.WorkspaceService.repository.TaskRepository;
import com.SwitchBoard.WorkspaceService.service.TaskAssignmentService;
import com.SwitchBoard.WorkspaceService.Exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskAssignmentServiceImpl implements TaskAssignmentService {

    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public List<TaskAssignmentResponse> assignUsersToTask(UUID taskId, TaskUserAssignmentRequest request) {
        log.info("TaskAssignmentServiceImpl :: assignUsersToTask :: Assigning {} users to task :: {}", 
                request.getUserIds().size(), taskId);

        // Validate task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        List<TaskAssignment> assignments = request.getUserIds().stream()
                .filter(userId -> !isUserAlreadyAssigned(taskId, userId)) // Skip already assigned users
                .map(userId -> TaskAssignment.builder()
                        .task(task)
                        .assignedUserId(userId)
                        .assignedByUserId(request.getAssignedBy())
                        .status(TaskStatus.ONGOING)
                        .assignedAt(Instant.now())
                        .build())
                .collect(Collectors.toList());

        List<TaskAssignment> savedAssignments = taskAssignmentRepository.saveAll(assignments);
        
        log.info("TaskAssignmentServiceImpl :: assignUsersToTask :: {} users assigned to task :: {}", 
                savedAssignments.size(), taskId);

        return savedAssignments.stream()
                .map(this::convertToTaskAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void unassignUsersFromTask(UUID taskId, List<UUID> userIds) {
        log.info("TaskAssignmentServiceImpl :: unassignUsersFromTask :: Unassigning {} users from task :: {}", 
                userIds.size(), taskId);

        // Validate task exists
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found with ID: " + taskId);
        }

        for (UUID userId : userIds) {
            taskAssignmentRepository.deleteByTaskIdAndAssignedUserId(taskId, userId);
        }

        log.info("TaskAssignmentServiceImpl :: unassignUsersFromTask :: {} users unassigned from task :: {}", 
                userIds.size(), taskId);
    }

    @Transactional
    public TaskAssignmentResponse updateTaskAssignment(UUID assignmentId, TaskAssignmentUpdateRequest request) {
        log.info("TaskAssignmentServiceImpl :: updateTaskAssignment :: Updating task assignment :: {}", assignmentId);

        TaskAssignment assignment = taskAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Task assignment not found with ID: " + assignmentId));

        // Update fields if provided
        if (request.getStatus() != null) {
            assignment.setStatus(request.getStatus());
            
            // Update completion time if marked as completed
            if (request.getStatus() == TaskStatus.COMPLETED && assignment.getCompletedAt() == null) {
                assignment.setCompletedAt(Instant.now());
                // Award reward points if task has them
                if (assignment.getTask().getRewardPoints() != null) {
                    assignment.setRewardPointsEarned(assignment.getTask().getRewardPoints());
                }
            }
            
            // Set started time if moving from TODO to IN_PROGRESS
            if (request.getStatus() == TaskStatus.ONGOING && assignment.getStartedAt() == null) {
                assignment.setStartedAt(Instant.now());
            }
        }


        if (request.getUserNotes() != null) {
            assignment.setUserNotes(request.getUserNotes());
        }

        if (request.getSubmissionText() != null) {
            assignment.setSubmissionText(request.getSubmissionText());
        }

        if (request.getSubmissionUrl() != null) {
            assignment.setSubmissionUrl(request.getSubmissionUrl());
        }

        if (request.getSubmissionStatus() != null) {
            assignment.setSubmissionStatus(request.getSubmissionStatus());
        }

        if (request.getGradeReceived() != null) {
            assignment.setGradeReceived(request.getGradeReceived());
        }

        if (request.getFeedback() != null) {
            assignment.setFeedback(request.getFeedback());
        }

        TaskAssignment updatedAssignment = taskAssignmentRepository.save(assignment);
        log.info("TaskAssignmentServiceImpl :: updateTaskAssignment :: Task assignment updated :: {}", assignmentId);

        return convertToTaskAssignmentResponse(updatedAssignment);
    }

    @Transactional(readOnly = true)
    public List<TaskAssignmentResponse> getTaskAssignmentsByTaskId(UUID taskId) {
        log.info("TaskAssignmentServiceImpl :: getTaskAssignmentsByTaskId :: Fetching assignments for task :: {}", taskId);

        List<TaskAssignment> assignments = taskAssignmentRepository.findByTaskId(taskId);
        return assignments.stream()
                .map(this::convertToTaskAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskAssignmentResponse> getTaskAssignmentsByUserId(UUID userId) {
        log.info("TaskAssignmentServiceImpl :: getTaskAssignmentsByUserId :: Fetching assignments for user :: {}", userId);

        List<TaskAssignment> assignments = taskAssignmentRepository.findByAssignedUserId(userId);
        return assignments.stream()
                .map(this::convertToTaskAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskAssignmentResponse getTaskAssignmentByTaskAndUser(UUID taskId, UUID userId) {
        log.info("TaskAssignmentServiceImpl :: getTaskAssignmentByTaskAndUser :: Fetching assignment :: task: {}, user: {}", 
                taskId, userId);

        TaskAssignment assignment = taskAssignmentRepository.findByTaskIdAndAssignedUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task assignment not found for task: " + taskId + " and user: " + userId));

        return convertToTaskAssignmentResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<TaskAssignmentResponse> getTaskAssignmentsByUserAndStatus(UUID userId, TaskStatus status) {
        log.info("TaskAssignmentServiceImpl :: getTaskAssignmentsByUserAndStatus :: Fetching assignments :: user: {}, status: {}", 
                userId, status);

        List<TaskAssignment> assignments = taskAssignmentRepository.findByAssignedUserIdAndStatus(userId, status);
        return assignments.stream()
                .map(this::convertToTaskAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskAssignmentResponse> getOverdueTaskAssignments() {
        log.info("TaskAssignmentServiceImpl :: getOverdueTaskAssignments :: Fetching overdue assignments");

        List<TaskAssignment> assignments = taskAssignmentRepository.findOverdueAssignments(Instant.now(), TaskStatus.COMPLETED);
        return assignments.stream()
                .map(this::convertToTaskAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isUserAlreadyAssigned(UUID taskId, UUID userId) {
        return taskAssignmentRepository.findByTaskIdAndAssignedUserId(taskId, userId).isPresent();
    }

    @Transactional
    public void deleteAllTaskAssignments(UUID taskId) {
        log.info("TaskAssignmentServiceImpl :: deleteAllTaskAssignments :: Deleting all assignments for task :: {}", taskId);
        taskAssignmentRepository.deleteByTaskId(taskId);
    }

    private TaskAssignmentResponse convertToTaskAssignmentResponse(TaskAssignment assignment) {
        return TaskAssignmentResponse.builder()
                .id(assignment.getId())
                .taskId(assignment.getTask().getId())
                .taskTitle(assignment.getTask().getTitle())
                .assignedUserId(assignment.getAssignedUserId())
                .assignedByUserId(assignment.getAssignedByUserId())
                .status(assignment.getStatus())
                .rewardPointsEarned(assignment.getRewardPointsEarned())
                .startedAt(assignment.getStartedAt())
                .completedAt(assignment.getCompletedAt())
                .assignedAt(assignment.getAssignedAt())
                .userNotes(assignment.getUserNotes())
                .submissionText(assignment.getSubmissionText())
                .submissionUrl(assignment.getSubmissionUrl())
                .submissionStatus(assignment.getSubmissionStatus())
                .gradeReceived(assignment.getGradeReceived())
                .feedback(assignment.getFeedback())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}