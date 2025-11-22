package com.SwitchBoard.WorkspaceService.service;

import com.SwitchBoard.WorkspaceService.dto.request.AssignmentCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentUpdateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.AssignmentResponse;
import com.SwitchBoard.WorkspaceService.dto.response.TaskResponse;
import com.SwitchBoard.WorkspaceService.entity.*;
import com.SwitchBoard.WorkspaceService.repository.AssignmentRepository;
import com.SwitchBoard.WorkspaceService.repository.TaskRepository;
import com.SwitchBoard.WorkspaceService.repository.WorkspaceRepository;
import com.SwitchBoard.WorkspaceService.Exception.ResourceNotFoundException;
import com.SwitchBoard.WorkspaceService.Exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TaskService taskService;
    private final TaskAssignmentService taskAssignmentService;

    @Transactional
    public AssignmentResponse createAssignment(AssignmentCreateRequest request) {
        log.info("AssignmentService :: createAssignment :: Creating assignment :: {}", request.getTitle());

        // Validate workspace exists
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + request.getWorkspaceId()));

        // Create assignment entity
        Assignment assignment = Assignment.builder()
                .workspace(workspace)
                .title(request.getTitle())
                .description(request.getDescription())
                .assignmentTypeKey(request.getAssignmentTypeKey())
                .totalRewardPoints(request.getTotalRewardPoints())
                .totalEstimatedHours(request.getTotalEstimatedHours())
                .deadline(request.getDeadline())
//                .roadmap(request.getRoadmap())getRoadmap
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("AssignmentService :: createAssignment :: Assignment created :: {}", savedAssignment.getId());

        // Handle task associations
        if (request.getTaskIds() != null && !request.getTaskIds().isEmpty()) {
            addTasksToAssignment(savedAssignment.getId(), request.getTaskIds());
        }

        // Handle new task creation
        if (request.getNewTasks() != null && !request.getNewTasks().isEmpty()) {
            for (TaskCreateRequest taskRequest : request.getNewTasks()) {
                taskRequest.setAssignmentId(savedAssignment.getId());
                taskRequest.setWorkspaceId(request.getWorkspaceId());
                taskService.createTask(taskRequest);
            }
        }

        return convertToAssignmentResponse(savedAssignment, true);
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(UUID id) {
        log.info("AssignmentService :: getAssignmentById :: Fetching assignment :: {}", id);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + id));

        return convertToAssignmentResponse(assignment, true);
    }

    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getAllAssignments(Pageable pageable) {
        log.info("AssignmentService :: getAllAssignments :: Fetching assignments :: page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Assignment> assignments = assignmentRepository.findAll(pageable);
        return assignments.map(assignment -> convertToAssignmentResponse(assignment, false));
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByWorkspaceId(UUID workspaceId) {
        log.info("AssignmentService :: getAssignmentsByWorkspaceId :: Fetching assignments for workspace :: {}", workspaceId);

        List<Assignment> assignments = assignmentRepository.findByWorkspaceId(workspaceId);
        return assignments.stream()
                .map(assignment -> convertToAssignmentResponse(assignment, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByType(AssignmentType assignmentType) {
        log.info("AssignmentService :: getAssignmentsByType :: Fetching assignments by type :: {}", assignmentType);

        List<Assignment> assignments = assignmentRepository.findByAssignmentTypeKey(assignmentType);
        return assignments.stream()
                .map(assignment -> convertToAssignmentResponse(assignment, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getOverdueAssignments() {
        log.info("AssignmentService :: getOverdueAssignments :: Fetching overdue assignments");

        List<Assignment> assignments = assignmentRepository.findOverdueAssignments(Instant.now());
        return assignments.stream()
                .map(assignment -> convertToAssignmentResponse(assignment, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public AssignmentResponse updateAssignment(UUID id, AssignmentUpdateRequest request) {
        log.info("AssignmentService :: updateAssignment :: Updating assignment :: {}", id);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + id));

        // Update fields
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setAssignmentTypeKey(request.getAssignmentTypeKey());
        assignment.setTotalRewardPoints(request.getTotalRewardPoints());
        assignment.setTotalEstimatedHours(request.getTotalEstimatedHours());
        assignment.setDeadline(request.getDeadline());
//        assignment.setRoadmapId(request.getRoadmapId());

        Assignment updatedAssignment = assignmentRepository.save(assignment);
        log.info("AssignmentService :: updateAssignment :: Assignment updated :: {}", updatedAssignment.getId());

        return convertToAssignmentResponse(updatedAssignment, true);
    }

    @Transactional
    public void deleteAssignment(UUID id) {
        log.info("AssignmentService :: deleteAssignment :: Deleting assignment :: {}", id);

        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + id));

        // Remove assignment reference from all tasks
        List<Task> tasks = taskRepository.findByAssignmentId(id);
        for (Task task : tasks) {
            task.setAssignment(null);
        }
        taskRepository.saveAll(tasks);

        assignmentRepository.delete(assignment);
        log.info("AssignmentService :: deleteAssignment :: Assignment deleted :: {}", id);
    }

    @Transactional
    public AssignmentResponse addTasksToAssignment(UUID assignmentId, List<UUID> taskIds) {
        log.info("AssignmentService :: addTasksToAssignment :: Adding {} tasks to assignment :: {}", 
                taskIds.size(), assignmentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        List<Task> tasks = taskRepository.findAllById(taskIds);
        if (tasks.size() != taskIds.size()) {
            throw new BadRequestException("One or more tasks not found");
        }

        // Validate all tasks belong to the same workspace
        UUID workspaceId = assignment.getWorkspace().getId();
        for (Task task : tasks) {
            if (!task.getWorkspace().getId().equals(workspaceId)) {
                throw new BadRequestException("Task " + task.getId() + " does not belong to the same workspace");
            }
            task.setAssignment(assignment);
        }

        taskRepository.saveAll(tasks);
        log.info("AssignmentService :: addTasksToAssignment :: {} tasks added to assignment :: {}", 
                tasks.size(), assignmentId);

        return convertToAssignmentResponse(assignment, true);
    }

    @Transactional
    public AssignmentResponse removeTasksFromAssignment(UUID assignmentId, List<UUID> taskIds) {
        log.info("AssignmentService :: removeTasksFromAssignment :: Removing {} tasks from assignment :: {}", 
                taskIds.size(), assignmentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        List<Task> tasks = taskRepository.findAllById(taskIds);
        for (Task task : tasks) {
            if (task.getAssignment() != null && task.getAssignment().getId().equals(assignmentId)) {
                task.setAssignment(null);
            }
        }

        taskRepository.saveAll(tasks);
        log.info("AssignmentService :: removeTasksFromAssignment :: {} tasks removed from assignment :: {}", 
                tasks.size(), assignmentId);

        return convertToAssignmentResponse(assignment, true);
    }

    @Transactional
    public AssignmentResponse assignUsersToAllTasks(UUID assignmentId, List<UUID> userIds, UUID assignedBy) {
        log.info("AssignmentService :: assignUsersToAllTasks :: Assigning {} users to all tasks in assignment :: {}", 
                userIds.size(), assignmentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        // Get all tasks in the assignment
        List<Task> tasks = taskRepository.findByAssignmentId(assignmentId);
        
        if (tasks.isEmpty()) {
            throw new BadRequestException("No tasks found in assignment: " + assignmentId);
        }

        // Create assignment request for each task
        com.SwitchBoard.WorkspaceService.dto.request.TaskUserAssignmentRequest assignmentRequest = 
                com.SwitchBoard.WorkspaceService.dto.request.TaskUserAssignmentRequest.builder()
                        .userIds(userIds)
                        .assignedBy(assignedBy)
                        .build();

        // Assign users to each task
        int totalAssignments = 0;
        for (Task task : tasks) {
            List<com.SwitchBoard.WorkspaceService.dto.response.TaskAssignmentResponse> taskAssignments = 
                    taskAssignmentService.assignUsersToTask(task.getId(), assignmentRequest);
            totalAssignments += taskAssignments.size();
        }

        log.info("AssignmentService :: assignUsersToAllTasks :: {} total user assignments created for assignment :: {}", 
                totalAssignments, assignmentId);

        return convertToAssignmentResponse(assignment, true);
    }

    @Transactional
    public void unassignUsersFromAllTasks(UUID assignmentId, List<UUID> userIds) {
        log.info("AssignmentService :: unassignUsersFromAllTasks :: Unassigning {} users from all tasks in assignment :: {}", 
                userIds.size(), assignmentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        // Get all tasks in the assignment
        List<Task> tasks = taskRepository.findByAssignmentId(assignmentId);

        // Unassign users from each task
        for (Task task : tasks) {
            taskAssignmentService.unassignUsersFromTask(task.getId(), userIds);
        }

        log.info("AssignmentService :: unassignUsersFromAllTasks :: {} users unassigned from all tasks in assignment :: {}", 
                userIds.size(), assignmentId);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAssignmentId(UUID assignmentId) {
        log.info("AssignmentService :: getTasksByAssignmentId :: Fetching tasks for assignment :: {}", assignmentId);

        // Verify assignment exists
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("Assignment not found with ID: " + assignmentId);
        }

        List<Task> tasks = taskRepository.findByAssignmentId(assignmentId);
        return tasks.stream()
                .map(this::convertToTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> searchAssignmentsByTitle(UUID workspaceId, String title) {
        log.info("AssignmentService :: searchAssignmentsByTitle :: Searching assignments in workspace :: {} with title :: {}", 
                workspaceId, title);

        List<Assignment> assignments = assignmentRepository.findByWorkspaceIdAndTitleContainingIgnoreCase(workspaceId, title);
        return assignments.stream()
                .map(assignment -> convertToAssignmentResponse(assignment, false))
                .collect(Collectors.toList());
    }

    private AssignmentResponse convertToAssignmentResponse(Assignment assignment, boolean includeTasks) {
        // Calculate statistics
        Long totalTasks = assignmentRepository.countTasksByAssignmentId(assignment.getId());
        Long completedTasks = assignmentRepository.countCompletedTasksByAssignmentId(assignment.getId());
        Integer pendingTasks = (int) (totalTasks - completedTasks);
        Double completionPercentage = totalTasks > 0 ? (completedTasks.doubleValue() / totalTasks) * 100 : 0.0;

        // Calculate total spent hours
        List<Task> tasks = taskRepository.findByAssignmentId(assignment.getId());
        Double totalSpentHours = tasks.stream()
                .mapToDouble(task -> task.getSpentHours() != null ? task.getSpentHours() : 0.0)
                .sum();

        AssignmentResponse.AssignmentResponseBuilder builder = AssignmentResponse.builder()
                .id(assignment.getId())
                .workspaceId(assignment.getWorkspace().getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .assignmentTypeKey(assignment.getAssignmentTypeKey())
                .totalRewardPoints(assignment.getTotalRewardPoints())
                .totalEstimatedHours(assignment.getTotalEstimatedHours())
                .deadline(assignment.getDeadline())
//                .roadmapId(assignment.getRoadmapId())
                .totalTasks(totalTasks.intValue())
                .completedTasks(completedTasks.intValue())
                .pendingTasks(pendingTasks)
                .completionPercentage(completionPercentage)
                .totalSpentHours(totalSpentHours)
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt());
//                .createdBy(assignment.getCreatedBy())
//                .updatedBy(assignment.getUpdatedBy());

        if (includeTasks) {
            List<TaskResponse> taskResponses = tasks.stream()
                    .map(this::convertToTaskResponse)
                    .collect(Collectors.toList());
            builder.tasks(taskResponses);
        }

        return builder.build();
    }

    private TaskResponse convertToTaskResponse(Task task) {
        // This is a simplified conversion. You should use your existing TaskService method
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .statusKey(task.getStatusKey())
                .priority(task.getPriority())
                .rewardPoints(task.getRewardPoints())
                .estimatedHours(task.getEstimatedHours())
                .spentHours(task.getSpentHours())
                .deadline(task.getDeadline())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}