package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.request.TaskCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskDto;
import com.SwitchBoard.WorkspaceService.dto.response.TaskResponse;
import com.SwitchBoard.WorkspaceService.entity.*;
import com.SwitchBoard.WorkspaceService.Exception.BadRequestException;
import com.SwitchBoard.WorkspaceService.Exception.ResourceNotFoundException;
import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public List<Task> createTask(TaskCreateRequest request) {
        log.info("TaskServiceImpl :: createTask :: Creating tasks");
        List<Task> tasks= request.getTasks().stream().map(
            taskDto -> {
                return Task.builder()
                        .title(taskDto.getTitle())
                        .description(taskDto.getDescription())
                        .statusKey(taskDto.getStatusKey())
                        .priority(taskDto.getPriority())
                        .rewardPoints(taskDto.getRewardPoints())
                        .estimatedHours(taskDto.getEstimatedHours())
                        .titleColor(taskDto.getTitleColor())
                        .deadline(taskDto.getDeadline())
                        .build();
                    }
                ).toList();
        List<Task> savedTasks = taskRepository.saveAll(tasks);
       log.info("TaskServiceImpl :: createTask :: Task saved successfully in DB");
        return savedTasks;
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
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        log.info("TaskServiceImpl :: getTasksByStatus :: Fetching tasks by status :: {}", status);

        List<Task> tasks = taskRepository.findByStatusKey(status);
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
    public TaskResponse updateTask(UUID id, TaskCreateRequest request) {
        log.info("TaskServiceImpl :: updateTask :: Updating task :: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("TaskServiceImpl :: updateTask :: Task not found :: {}", id);
                    return new ResourceNotFoundException("Task not found with id: " + id);
                });
        TaskDto taskDto = request.getTasks().get(0);

        // Update basic fields
        if (taskDto.getTitle() != null) {
            task.setTitle(taskDto.getTitle());
        }
        if (taskDto.getDescription() != null) {
            task.setDescription(taskDto.getDescription());
        }

        if (taskDto.getStatusKey() != null) {
            task.setStatusKey(taskDto.getStatusKey());
        }
        if (taskDto.getPriority() != null) {
            task.setPriority(taskDto.getPriority());
        }
        if (taskDto.getRewardPoints() != null) {
            task.setRewardPoints(taskDto.getRewardPoints());
        }
        if (taskDto.getEstimatedHours() != null) {
            task.setEstimatedHours(taskDto.getEstimatedHours());
        }

        if (taskDto.getTitleColor() != null) {
            task.setTitleColor(taskDto.getTitleColor());
        }

        if (taskDto.getDeadline() != null) {
            task.setDeadline(taskDto.getDeadline());
        }

        if (request.getAssigneeUserId() != null) {
            task.setAssigneeUserId(request.getAssigneeUserId());
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


    private TaskResponse mapToTaskResponse(Task task) {
        TaskResponse.TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .statusKey(task.getStatusKey())
                .priority(task.getPriority())
                .rewardPoints(task.getRewardPoints())
                .estimatedHours(task.getEstimatedHours())
                .titleColor(task.getTitleColor())
                .deadline(task.getDeadline())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt());

        // Set assignee UUID if present
        if (task.getAssigneeUserId() != null) {
            builder.assigneeUserId(task.getAssigneeUserId());
        }

        // Set reporter UUID if present
        if (task.getReporterUserId() != null) {
            builder.reporterUserId(task.getReporterUserId());
        }

        Integer commentCount = task.getComments() != null ? task.getComments().size() : 0;
        builder.commentCount(commentCount);

        return builder.build();
    }

}