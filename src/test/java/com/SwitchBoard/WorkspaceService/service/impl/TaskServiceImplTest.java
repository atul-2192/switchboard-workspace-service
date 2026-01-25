package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.Exception.BadRequestException;
import com.SwitchBoard.WorkspaceService.Exception.ResourceNotFoundException;
import com.SwitchBoard.WorkspaceService.dto.request.TaskCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskDto;
import com.SwitchBoard.WorkspaceService.dto.response.TaskResponse;
import com.SwitchBoard.WorkspaceService.entity.Task;
import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import com.SwitchBoard.WorkspaceService.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void createTask_validRequest_returnsCreatedTasks() {
        // Arrange
        TaskDto dto = new TaskDto();
        dto.setTitle("Test Task");
        dto.setDescription("Desc");
        dto.setPriority(3);
        dto.setStatusKey(TaskStatus.BACKLOG);

        TaskCreateRequest request = TaskCreateRequest.builder()
                .tasks(List.of(dto))
                .build();

        Task savedTask = Task.builder()
                .title("Test Task")
                .build();

        when(taskRepository.saveAll(anyList())).thenReturn(List.of(savedTask));

        // Act
        List<Task> result = taskService.createTask(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getTaskById_existingId_returnsTask() {
        // Arrange
        UUID id = UUID.randomUUID();
        Task task = Task.builder()
                .title("Task 1")
                .statusKey(TaskStatus.BACKLOG)
                .build();

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        // Act
        TaskResponse response = taskService.getTaskById(id);

        // Assert
        assertNotNull(response);
        assertEquals("Task 1", response.getTitle());
    }

    @Test
    void getTaskById_nonExistingId_throwsException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(id));
    }

    @Test
    void getAllTasks_withPagination_returnsPageOfTasks() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Task task = Task.builder().title("Task").build();
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(pageable)).thenReturn(taskPage);

        // Act
        Page<TaskResponse> result = taskService.getAllTasks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateTaskStatus_validTransition_updatesStatus() {
        // Arrange
        UUID id = UUID.randomUUID();
        Task task = Task.builder()
                .statusKey(TaskStatus.BACKLOG)
                .build();

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        TaskResponse response = taskService.updateTaskStatus(id, TaskStatus.ONGOING);

        // Assert
        assertNotNull(response);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void addTimeSpent_negativeHours_throwsException() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act & Assert
        assertThrows(BadRequestException.class, () -> taskService.addTimeSpent(id, -5.0));
    }

    @Test
    void deleteTask_existingId_deletesTask() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(taskRepository.existsById(id)).thenReturn(true);

        // Act
        taskService.deleteTask(id);

        // Assert
        verify(taskRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteTask_nonExistingId_throwsException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(taskRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(id));
    }

    @Test
    void getTasksByStatus_validStatus_returnsList() {
        // Arrange
        Task task = Task.builder().statusKey(TaskStatus.COMPLETED).build();
        when(taskRepository.findByStatusKey(TaskStatus.COMPLETED)).thenReturn(List.of(task));

        // Act
        List<TaskResponse> result = taskService.getTasksByStatus(TaskStatus.COMPLETED);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOverdueTasks_returnsOverdueTasks() {
        // Arrange
        Task task = Task.builder()
                .deadline(Instant.now().minusSeconds(86400))
                .build();

        when(taskRepository.findOverdueTasks(any(Instant.class), eq(TaskStatus.COMPLETED)))
                .thenReturn(List.of(task));

        // Act
        List<TaskResponse> result = taskService.getOverdueTasks();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
