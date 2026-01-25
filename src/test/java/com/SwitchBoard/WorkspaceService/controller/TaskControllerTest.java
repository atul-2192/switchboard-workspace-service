package com.SwitchBoard.WorkspaceService.controller;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.request.TaskCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskDto;
import com.SwitchBoard.WorkspaceService.dto.response.TaskResponse;
import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import com.SwitchBoard.WorkspaceService.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @Test
    void getTaskById_returnsTask() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        TaskResponse response = TaskResponse.builder()
                .id(taskId)
                .title("Test Task")
                .build();

        when(taskService.getTaskById(taskId)).thenReturn(response);

        // Act
        ResponseEntity<TaskResponse> result = taskController.getTaskById(taskId);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("Test Task", result.getBody().getTitle());
        verify(taskService, times(1)).getTaskById(taskId);
    }

    @Test
    void getAllTasks_returnsPaginatedTasks() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        TaskResponse task = TaskResponse.builder().title("Task").build();
        Page<TaskResponse> page = new PageImpl<>(List.of(task));

        when(taskService.getAllTasks(pageable)).thenReturn(page);

        // Act
        ResponseEntity<Page<TaskResponse>> result = taskController.getAllTasks(pageable);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getTotalElements());
    }

    @Test
    void getTasksByAssignmentId_returnsTasks() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        TaskResponse task = TaskResponse.builder().title("Task").build();

        when(taskService.getTasksByAssignmentId(assignmentId)).thenReturn(List.of(task));

        // Act
        ResponseEntity<List<TaskResponse>> result = taskController.getTasksByAssignmentId(assignmentId);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getTasksByAssigneeId_withValidHeader_returnsTasks() {
        // Arrange
        UUID userId = UUID.randomUUID();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-User-Id")).thenReturn(userId.toString());

        TaskResponse task = TaskResponse.builder().title("Assigned Task").build();
        when(taskService.getTasksByAssigneeId(userId)).thenReturn(List.of(task));

        // Act
        ResponseEntity<List<TaskResponse>> result = taskController.getTasksByAssigneeId(request);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getTasksByReporterId_withValidHeader_returnsTasks() {
        // Arrange
        UUID userId = UUID.randomUUID();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-User-Id")).thenReturn(userId.toString());

        TaskResponse task = TaskResponse.builder().title("Created Task").build();
        when(taskService.getTasksByReporterId(userId)).thenReturn(List.of(task));

        // Act
        ResponseEntity<List<TaskResponse>> result = taskController.getTasksByReporterId(request);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void updateTask_validRequest_returnsSuccess() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        TaskDto dto = new TaskDto();
        dto.setTitle("Updated Task");
        TaskCreateRequest request = TaskCreateRequest.builder()
                .tasks(List.of(dto))
                .build();

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/tasks/" + taskId);

        TaskResponse taskResponse = TaskResponse.builder().title("Updated Task").build();
        when(taskService.updateTask(taskId, request)).thenReturn(taskResponse);

        // Act
        ResponseEntity<ApiResponse> result = taskController.updateTask(taskId, request, httpRequest);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().isSuccess());
    }

    @Test
    void updateTaskStatus_validRequest_returnsSuccess() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        TaskStatus status = TaskStatus.COMPLETED;

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/tasks/" + taskId + "/status");

        TaskResponse taskResponse = TaskResponse.builder().statusKey(status).build();
        when(taskService.updateTaskStatus(taskId, status)).thenReturn(taskResponse);

        // Act
        ResponseEntity<ApiResponse> result = taskController.updateTaskStatus(taskId, status, httpRequest);

        // Assert
        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().isSuccess());
    }
}
