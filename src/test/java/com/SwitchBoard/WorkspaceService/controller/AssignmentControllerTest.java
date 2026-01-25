package com.SwitchBoard.WorkspaceService.controller;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentTaskManagementRequest;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentUpdateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.response.AssignmentResponse;
import com.SwitchBoard.WorkspaceService.dto.response.TaskResponse;
import com.SwitchBoard.WorkspaceService.entity.enums.AssignmentType;
import com.SwitchBoard.WorkspaceService.service.impl.AssignmentService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AssignmentController assignmentController;

    @Test
    void createAssignment_validRequest_returnsCreated() {
        // Arrange
        AssignmentCreateRequest request = new AssignmentCreateRequest();
        request.setTitle("New Assignment");
        request.setDescription("Description");
        
        AssignmentResponse assignmentResponse = AssignmentResponse.builder()
                .id(UUID.randomUUID())
                .title("New Assignment")
                .assignmentTypeKey(AssignmentType.CUSTOM)
                .build();
        
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/assignments");
        when(assignmentService.createAssignment(request)).thenReturn(assignmentResponse);

        // Act
        ResponseEntity<ApiResponse> response = assignmentController.createAssignment(request, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Assignment created successfully", response.getBody().getMessage());
        verify(assignmentService, times(1)).createAssignment(request);
    }

    @Test
    void getAssignmentById_existingId_returnsAssignment() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        AssignmentResponse expectedAssignment = AssignmentResponse.builder()
                .id(assignmentId)
                .title("Test Assignment")
                .totalRewardPoints(100)
                .build();
        when(assignmentService.getAssignmentById(assignmentId)).thenReturn(expectedAssignment);

        // Act
        ResponseEntity<AssignmentResponse> response = assignmentController.getAssignmentById(assignmentId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAssignment, response.getBody());
        verify(assignmentService, times(1)).getAssignmentById(assignmentId);
    }

    @Test
    void getAllAssignments_withPagination_returnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<AssignmentResponse> assignments = Arrays.asList(
                AssignmentResponse.builder().id(UUID.randomUUID()).title("Assignment 1").build(),
                AssignmentResponse.builder().id(UUID.randomUUID()).title("Assignment 2").build()
        );
        Page<AssignmentResponse> expectedPage = new PageImpl<>(assignments, pageable, 2);
        when(assignmentService.getAllAssignments(pageable)).thenReturn(expectedPage);

        // Act
        ResponseEntity<Page<AssignmentResponse>> response = assignmentController.getAllAssignments(pageable);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalElements());
        verify(assignmentService, times(1)).getAllAssignments(pageable);
    }

    @Test
    void getAssignmentsByWorkspaceId_validWorkspaceId_returnsAssignments() {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        List<AssignmentResponse> expectedAssignments = Arrays.asList(
                AssignmentResponse.builder().id(UUID.randomUUID()).title("Assignment 1").build(),
                AssignmentResponse.builder().id(UUID.randomUUID()).title("Assignment 2").build(),
                AssignmentResponse.builder().id(UUID.randomUUID()).title("Assignment 3").build()
        );
        when(assignmentService.getAssignmentsByWorkspaceId(workspaceId)).thenReturn(expectedAssignments);

        // Act
        ResponseEntity<List<AssignmentResponse>> response = assignmentController.getAssignmentsByWorkspaceId(workspaceId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(assignmentService, times(1)).getAssignmentsByWorkspaceId(workspaceId);
    }

    @Test
    void getOverdueAssignments_returnsOverdueList() {
        // Arrange
        List<AssignmentResponse> overdueAssignments = Arrays.asList(
                AssignmentResponse.builder()
                        .id(UUID.randomUUID())
                        .title("Overdue Assignment 1")
                        .deadline(Instant.now().minusSeconds(86400))
                        .build(),
                AssignmentResponse.builder()
                        .id(UUID.randomUUID())
                        .title("Overdue Assignment 2")
                        .deadline(Instant.now().minusSeconds(172800))
                        .build()
        );
        when(assignmentService.getOverdueAssignments()).thenReturn(overdueAssignments);

        // Act
        ResponseEntity<List<AssignmentResponse>> response = assignmentController.getOverdueAssignments();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(assignmentService, times(1)).getOverdueAssignments();
    }

    @Test
    void updateAssignment_validRequest_returnsOk() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        AssignmentUpdateRequest request = new AssignmentUpdateRequest();
        request.setTitle("Updated Assignment");
        
        AssignmentResponse updatedAssignment = AssignmentResponse.builder()
                .id(assignmentId)
                .title("Updated Assignment")
                .build();
        
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/assignments/" + assignmentId);
        when(assignmentService.updateAssignment(assignmentId, request)).thenReturn(updatedAssignment);

        // Act
        ResponseEntity<ApiResponse> response = assignmentController.updateAssignment(assignmentId, request, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Assignment updated successfully", response.getBody().getMessage());
        verify(assignmentService, times(1)).updateAssignment(assignmentId, request);
    }

    @Test
    void deleteAssignment_existingId_returnsOk() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/assignments/" + assignmentId);
        doNothing().when(assignmentService).deleteAssignment(assignmentId);

        // Act
        ResponseEntity<ApiResponse> response = assignmentController.deleteAssignment(assignmentId, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Assignment deleted successfully", response.getBody().getMessage());
        verify(assignmentService, times(1)).deleteAssignment(assignmentId);
    }

    @Test
    void getTasksByAssignmentId_validId_returnsTasks() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        List<TaskResponse> expectedTasks = Arrays.asList(
                TaskResponse.builder().id(UUID.randomUUID()).title("Task 1").build(),
                TaskResponse.builder().id(UUID.randomUUID()).title("Task 2").build()
        );
        when(assignmentService.getTasksByAssignmentId(assignmentId)).thenReturn(expectedTasks);

        // Act
        ResponseEntity<List<TaskResponse>> response = assignmentController.getTasksByAssignmentId(assignmentId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(assignmentService, times(1)).getTasksByAssignmentId(assignmentId);
    }

    @Test
    void addTasksToAssignment_validRequest_returnsOk() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        TaskCreateRequest request = new TaskCreateRequest();
        
        AssignmentResponse updatedAssignment = AssignmentResponse.builder()
                .id(assignmentId)
                .totalTasks(2)
                .build();
        
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/assignments/" + assignmentId + "/tasks");
        when(assignmentService.addTasksToAssignment(assignmentId, request)).thenReturn(updatedAssignment);

        // Act
        ResponseEntity<ApiResponse> response = assignmentController.addTasksToAssignment(assignmentId, request, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(assignmentService, times(1)).addTasksToAssignment(assignmentId, request);
    }

    @Test
    void removeTasksFromAssignment_validRequest_returnsOk() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        AssignmentTaskManagementRequest request = new AssignmentTaskManagementRequest();
        request.setTaskIds(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
        
        AssignmentResponse updatedAssignment = AssignmentResponse.builder()
                .id(assignmentId)
                .totalTasks(0)
                .build();
        
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/assignments/" + assignmentId + "/tasks");
        when(assignmentService.removeTasksFromAssignment(assignmentId, request.getTaskIds())).thenReturn(updatedAssignment);

        // Act
        ResponseEntity<ApiResponse> response = assignmentController.removeTasksFromAssignment(assignmentId, request, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tasks removed from assignment successfully", response.getBody().getMessage());
        verify(assignmentService, times(1)).removeTasksFromAssignment(assignmentId, request.getTaskIds());
    }

    @Test
    void assignUsersToAllTasks_validRequest_returnsOk() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        List<UUID> userIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        
        AssignmentResponse updatedAssignment = AssignmentResponse.builder()
                .id(assignmentId)
                .build();
        
        when(httpRequest.getHeader("X-User-Id")).thenReturn(userId.toString());
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/assignments/" + assignmentId);
        when(assignmentService.assignUsersToAllTasks(assignmentId, userIds, userId)).thenReturn(updatedAssignment);

        // Act
        ResponseEntity<ApiResponse> response = assignmentController.assignUsersToAllTasks(assignmentId, userIds, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Users successfully assigned to all tasks in assignment", response.getBody().getMessage());
        verify(assignmentService, times(1)).assignUsersToAllTasks(assignmentId, userIds, userId);
    }

    @Test
    void assignUsersToAllTasks_noUserIdHeader_usesNullAssignedBy() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        List<UUID> userIds = Arrays.asList(UUID.randomUUID());
        
        AssignmentResponse updatedAssignment = AssignmentResponse.builder()
                .id(assignmentId)
                .build();
        
        when(httpRequest.getHeader("X-User-Id")).thenReturn(null);
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/assignments");
        when(assignmentService.assignUsersToAllTasks(assignmentId, userIds, null)).thenReturn(updatedAssignment);

        // Act
        ResponseEntity<ApiResponse> response = assignmentController.assignUsersToAllTasks(assignmentId, userIds, httpRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(assignmentService, times(1)).assignUsersToAllTasks(assignmentId, userIds, null);
    }

    @Test
    void unassignUsersFromAllTasks_validRequest_returnsOk() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        List<UUID> userIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        
        when(httpRequest.getRequestURI()).thenReturn("/api/v1/assignments/" + assignmentId);
        doNothing().when(assignmentService).unassignUsersFromAllTasks(assignmentId, userIds);

        // Act
        ResponseEntity<ApiResponse> response = assignmentController.unassignUsersFromAllTasks(assignmentId, userIds, httpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Users successfully unassigned from all tasks in assignment", response.getBody().getMessage());
        verify(assignmentService, times(1)).unassignUsersFromAllTasks(assignmentId, userIds);
    }
}
