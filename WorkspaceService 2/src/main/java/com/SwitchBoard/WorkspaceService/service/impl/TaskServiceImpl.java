package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.dto.request.TaskCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.TaskResponse;
import com.SwitchBoard.WorkspaceService.dto.response.TagResponse;
import com.SwitchBoard.WorkspaceService.entity.*;
import com.SwitchBoard.WorkspaceService.Exception.BadRequestException;
import com.SwitchBoard.WorkspaceService.Exception.ResourceNotFoundException;
import com.SwitchBoard.WorkspaceService.repository.*;
import com.SwitchBoard.WorkspaceService.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AssignmentRepository assignmentRepository;
    private final TagRepository tagRepository;

    @Override
    public TaskResponse createTask(TaskCreateRequest request) {
        log.info("TaskServiceImpl :: createTask :: Creating task :: {}", request.getTitle());

        // Validate workspace exists
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> {
                    log.error("TaskServiceImpl :: createTask :: Workspace not found :: {}", request.getWorkspaceId());
                    return new ResourceNotFoundException("Workspace not found with id: " + request.getWorkspaceId());
                });

        Task.TaskBuilder taskBuilder = Task.builder()
                .workspace(workspace)
                .title(request.getTitle())
                .description(request.getDescription())
                .taskTypeKey(request.getTaskTypeKey())
                .statusKey(request.getStatusKey() != null ? request.getStatusKey() : TaskStatus.BACKLOG)
                .priority(request.getPriority())
                .rewardPoints(request.getRewardPoints())
                .estimatedHours(request.getEstimatedHours())
                .spentHours(request.getSpentHours() != null ? request.getSpentHours() : 0.0)
                .titleColor(request.getTitleColor())
                .position(request.getPosition())
                .deadline(request.getDeadline());

        // Set assignment if provided
        if (request.getAssignmentId() != null) {
            Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                    .orElseThrow(() -> {
                        log.error("TaskServiceImpl :: createTask :: Assignment not found :: {}", request.getAssignmentId());
                        return new ResourceNotFoundException("Assignment not found with id: " + request.getAssignmentId());
                    });
            taskBuilder.assignment(assignment);
        }

        // Set parent task if provided
        if (request.getParentTaskId() != null) {
            Task parentTask = taskRepository.findById(request.getParentTaskId())
                    .orElseThrow(() -> {
                        log.error("TaskServiceImpl :: createTask :: Parent task not found :: {}", request.getParentTaskId());
                        return new ResourceNotFoundException("Parent task not found with id: " + request.getParentTaskId());
                    });
            taskBuilder.parentTask(parentTask);
        }

        // Set assignee if provided
        if (request.getAssigneeUserId() != null) {
            taskBuilder.assigneeUserId(request.getAssigneeUserId());
        }

        // Set reporter if provided
        if (request.getReporterUserId() != null) {
            taskBuilder.reporterUserId(request.getReporterUserId());
        }

        Task task = taskBuilder.build();

        // Set tags if provided
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = request.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> {
                                log.error("TaskServiceImpl :: createTask :: Tag not found :: {}", tagId);
                                return new ResourceNotFoundException("Tag not found with id: " + tagId);
                            }))
                    .collect(Collectors.toSet());
            task.setTags(tags);
        }

        Task savedTask = taskRepository.save(task);
        log.info("TaskServiceImpl :: createTask :: Task created successfully :: {}", savedTask.getId());

        return mapToTaskResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id) {
        log.info("TaskServiceImpl :: getTaskById :: Fetching task :: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("TaskServiceImpl :: getTaskById :: Task not found :: {}", id);
                    return new ResourceNotFoundException("Task not found with id: " + id);
                });

        return mapToTaskResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        log.info("TaskServiceImpl :: getAllTasks :: Fetching all tasks :: page: {}, size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Task> tasks = taskRepository.findAll(pageable);
        return tasks.map(this::mapToTaskResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByWorkspaceId(UUID workspaceId) {
        log.info("TaskServiceImpl :: getTasksByWorkspaceId :: Fetching tasks for workspace :: {}", workspaceId);

        List<Task> tasks = taskRepository.findByWorkspaceId(workspaceId);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAssignmentId(UUID assignmentId) {
        log.info("TaskServiceImpl :: getTasksByAssignmentId :: Fetching tasks for assignment :: {}", assignmentId);

        List<Task> tasks = taskRepository.findByAssignmentId(assignmentId);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAssigneeId(UUID assigneeId) {
        log.info("TaskServiceImpl :: getTasksByAssigneeId :: Fetching tasks for assignee :: {}", assigneeId);

        List<Task> tasks = taskRepository.findByAssigneeUserId(assigneeId);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByReporterId(UUID reporterId) {
        log.info("TaskServiceImpl :: getTasksByReporterId :: Fetching tasks for reporter :: {}", reporterId);

        List<Task> tasks = taskRepository.findByReporterUserId(reporterId);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getSubTasks(UUID parentTaskId) {
        log.info("TaskServiceImpl :: getSubTasks :: Fetching subtasks for parent :: {}", parentTaskId);

        List<Task> tasks = taskRepository.findByParentTaskId(parentTaskId);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getRootTasksByWorkspaceId(UUID workspaceId) {
        log.info("TaskServiceImpl :: getRootTasksByWorkspaceId :: Fetching root tasks for workspace :: {}", workspaceId);

        List<Task> tasks = taskRepository.findRootTasksByWorkspaceId(workspaceId);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        log.info("TaskServiceImpl :: getTasksByStatus :: Fetching tasks by status :: {}", status);

        List<Task> tasks = taskRepository.findByStatusKey(status);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByWorkspaceAndStatus(UUID workspaceId, TaskStatus status) {
        log.info("TaskServiceImpl :: getTasksByWorkspaceAndStatus :: Fetching tasks :: workspace: {}, status: {}", 
                workspaceId, status);

        List<Task> tasks = taskRepository.findByWorkspaceIdAndStatusKey(workspaceId, status);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks() {
        log.info("TaskServiceImpl :: getOverdueTasks :: Fetching overdue tasks");

        List<Task> tasks = taskRepository.findOverdueTasks(Instant.now(), TaskStatus.COMPLETED);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> searchTasksByTitle(UUID workspaceId, String title) {
        log.info("TaskServiceImpl :: searchTasksByTitle :: Searching tasks :: workspace: {}, title: {}", 
                workspaceId, title);

        List<Task> tasks = taskRepository.findByWorkspaceIdAndTitleContainingIgnoreCase(workspaceId, title);
        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TaskResponse updateTask(UUID id, TaskCreateRequest request) {
        log.info("TaskServiceImpl :: updateTask :: Updating task :: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("TaskServiceImpl :: updateTask :: Task not found :: {}", id);
                    return new ResourceNotFoundException("Task not found with id: " + id);
                });

        // Update basic fields
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getTaskTypeKey() != null) {
            task.setTaskTypeKey(request.getTaskTypeKey());
        }
        if (request.getStatusKey() != null) {
            task.setStatusKey(request.getStatusKey());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getRewardPoints() != null) {
            task.setRewardPoints(request.getRewardPoints());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getSpentHours() != null) {
            task.setSpentHours(request.getSpentHours());
        }
        if (request.getTitleColor() != null) {
            task.setTitleColor(request.getTitleColor());
        }
        if (request.getPosition() != null) {
            task.setPosition(request.getPosition());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }

        // Update assignee if provided
        if (request.getAssigneeUserId() != null) {
            task.setAssigneeUserId(request.getAssigneeUserId());
        }

        // Update tags if provided
        if (request.getTagIds() != null) {
            Set<Tag> tags = request.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> {
                                log.error("TaskServiceImpl :: updateTask :: Tag not found :: {}", tagId);
                                return new ResourceNotFoundException("Tag not found with id: " + tagId);
                            }))
                    .collect(Collectors.toSet());
            task.setTags(tags);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("TaskServiceImpl :: updateTask :: Task updated successfully :: {}", updatedTask.getId());

        return mapToTaskResponse(updatedTask);
    }

    @Override
    public TaskResponse updateTaskStatus(UUID id, TaskStatus status) {
        log.info("TaskServiceImpl :: updateTaskStatus :: Updating task status :: id: {}, status: {}", id, status);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("TaskServiceImpl :: updateTaskStatus :: Task not found :: {}", id);
                    return new ResourceNotFoundException("Task not found with id: " + id);
                });

        TaskStatus previousStatus = task.getStatusKey();
        task.setStatusKey(status);

        // Set timestamps based on status
        if (status == TaskStatus.ONGOING && previousStatus != TaskStatus.ONGOING) {
            task.setStartedAt(Instant.now());
        } else if (status == TaskStatus.COMPLETED && previousStatus != TaskStatus.COMPLETED) {
            task.setCompletedAt(Instant.now());
        }

        Task updatedTask = taskRepository.save(task);
        log.info("TaskServiceImpl :: updateTaskStatus :: Task status updated successfully :: {}", id);

        return mapToTaskResponse(updatedTask);
    }

    @Override
    public TaskResponse assignTask(UUID id, UUID assigneeId) {
        log.info("TaskServiceImpl :: assignTask :: Assigning task :: id: {}, assignee: {}", id, assigneeId);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("TaskServiceImpl :: assignTask :: Task not found :: {}", id);
                    return new ResourceNotFoundException("Task not found with id: " + id);
                });

        task.setAssigneeUserId(assigneeId);
        Task updatedTask = taskRepository.save(task);
        log.info("TaskServiceImpl :: assignTask :: Task assigned successfully :: {}", id);

        return mapToTaskResponse(updatedTask);
    }

    @Override
    public TaskResponse addTimeSpent(UUID id, Double hours) {
        log.info("TaskServiceImpl :: addTimeSpent :: Adding time spent :: id: {}, hours: {}", id, hours);

        if (hours < 0) {
            log.error("TaskServiceImpl :: addTimeSpent :: Invalid hours value :: {}", hours);
            throw new BadRequestException("Hours spent cannot be negative");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("TaskServiceImpl :: addTimeSpent :: Task not found :: {}", id);
                    return new ResourceNotFoundException("Task not found with id: " + id);
                });

        double currentSpentHours = task.getSpentHours() != null ? task.getSpentHours() : 0.0;
        task.setSpentHours(currentSpentHours + hours);

        Task updatedTask = taskRepository.save(task);
        log.info("TaskServiceImpl :: addTimeSpent :: Time spent added successfully :: {}", id);

        return mapToTaskResponse(updatedTask);
    }

    @Override
    public void deleteTask(UUID id) {
        log.info("TaskServiceImpl :: deleteTask :: Deleting task :: {}", id);

        if (!taskRepository.existsById(id)) {
            log.error("TaskServiceImpl :: deleteTask :: Task not found :: {}", id);
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }

        taskRepository.deleteById(id);
        log.info("TaskServiceImpl :: deleteTask :: Task deleted successfully :: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getSubTaskCount(UUID parentTaskId) {
        log.info("TaskServiceImpl :: getSubTaskCount :: Counting subtasks :: {}", parentTaskId);

        Long count = taskRepository.countSubTasksByParentTaskId(parentTaskId);
        return count != null ? count : 0L;
    }

    private TaskResponse mapToTaskResponse(Task task) {
        TaskResponse.TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .workspaceId(task.getWorkspace().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskTypeKey(task.getTaskTypeKey())
                .statusKey(task.getStatusKey())
                .priority(task.getPriority())
                .rewardPoints(task.getRewardPoints())
                .estimatedHours(task.getEstimatedHours())
                .spentHours(task.getSpentHours())
                .titleColor(task.getTitleColor())
                .position(task.getPosition())
                .deadline(task.getDeadline())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt());

        // Set assignment ID if present
        if (task.getAssignment() != null) {
            builder.assignmentId(task.getAssignment().getId());
        }

        // Set parent task ID if present
        if (task.getParentTask() != null) {
            builder.parentTaskId(task.getParentTask().getId());
        }

        // Set assignee UUID if present
        if (task.getAssigneeUserId() != null) {
            builder.assigneeUserId(task.getAssigneeUserId());
        }

        // Set reporter UUID if present
        if (task.getReporterUserId() != null) {
            builder.reporterUserId(task.getReporterUserId());
        }

        // Set tags if present
        if (task.getTags() != null && !task.getTags().isEmpty()) {
            List<TagResponse> tagResponses = task.getTags().stream()
                    .map(this::mapToTagResponse)
                    .collect(Collectors.toList());
            builder.tags(tagResponses);
        }

        // Set counts
        Long subTaskCount = getSubTaskCount(task.getId());
        builder.subTaskCount(subTaskCount.intValue());

        Integer commentCount = task.getComments() != null ? task.getComments().size() : 0;
        builder.commentCount(commentCount);

        return builder.build();
    }

    private TagResponse mapToTagResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .workspaceId(tag.getWorkspace() != null ? tag.getWorkspace().getId() : null)
                .name(tag.getName())
                .color(tag.getColor())
                .description(tag.getDescription())
                .taskCount(tag.getTasks() != null ? tag.getTasks().size() : 0)
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}