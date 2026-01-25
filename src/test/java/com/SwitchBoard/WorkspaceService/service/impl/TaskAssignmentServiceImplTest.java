package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.Exception.ResourceNotFoundException;
import com.SwitchBoard.WorkspaceService.dto.request.TaskAssignmentUpdateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskUserAssignmentRequest;
import com.SwitchBoard.WorkspaceService.dto.response.TaskAssignmentResponse;
import com.SwitchBoard.WorkspaceService.entity.Task;
import com.SwitchBoard.WorkspaceService.entity.TaskAssignment;
import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import com.SwitchBoard.WorkspaceService.repository.TaskAssignmentRepository;
import com.SwitchBoard.WorkspaceService.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskAssignmentServiceImplTest {

    @Mock
    private TaskAssignmentRepository taskAssignmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskAssignmentServiceImpl taskAssignmentService;

    private UUID taskId;
    private UUID userId;
    private UUID assignedBy;
    private Task task;
    private TaskAssignment taskAssignment;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();
        assignedBy = UUID.randomUUID();

        task = Task.builder()
                .title("Test Task")
                .description("Test Description")
                .statusKey(TaskStatus.BACKLOG)
                .priority(1)
                .rewardPoints(10)
                .build();
        task.setId(taskId);

        taskAssignment = TaskAssignment.builder()
                .task(task)
                .assignedUserId(userId)
                .assignedByUserId(assignedBy)
                .status(TaskStatus.ONGOING)
                .assignedAt(Instant.now())
                .build();
        taskAssignment.setId(UUID.randomUUID());
    }

    @Test
    void assignUsersToTask_validRequest_assignsUsers() {
        // Arrange
        List<UUID> userIds = Arrays.asList(userId, UUID.randomUUID());
        TaskUserAssignmentRequest request = TaskUserAssignmentRequest.builder()
                .userIds(userIds)
                .assignedBy(assignedBy)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByTaskIdAndAssignedUserId(any(), any()))
                .thenReturn(Optional.empty());
        when(taskAssignmentRepository.saveAll(anyList()))
                .thenReturn(Arrays.asList(taskAssignment));

        // Act
        List<TaskAssignmentResponse> responses = taskAssignmentService.assignUsersToTask(taskId, request);

        // Assert
        assertNotNull(responses);
        assertFalse(responses.isEmpty());

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskAssignmentRepository, times(1)).saveAll(anyList());
    }

    @Test
    void assignUsersToTask_nonExistingTask_throwsException() {
        // Arrange
        TaskUserAssignmentRequest request = TaskUserAssignmentRequest.builder()
                .userIds(Arrays.asList(userId))
                .assignedBy(assignedBy)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskAssignmentService.assignUsersToTask(taskId, request));

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskAssignmentRepository, never()).saveAll(anyList());
    }

    @Test
    void assignUsersToTask_userAlreadyAssigned_skipsUser() {
        // Arrange
        TaskUserAssignmentRequest request = TaskUserAssignmentRequest.builder()
                .userIds(Arrays.asList(userId))
                .assignedBy(assignedBy)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findByTaskIdAndAssignedUserId(taskId, userId))
                .thenReturn(Optional.of(taskAssignment));
        when(taskAssignmentRepository.saveAll(anyList()))
                .thenReturn(Collections.emptyList());

        // Act
        List<TaskAssignmentResponse> responses = taskAssignmentService.assignUsersToTask(taskId, request);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty()); // User was already assigned, so skipped

        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    void unassignUsersFromTask_validRequest_unassignsUsers() {
        // Arrange
        List<UUID> userIds = Arrays.asList(userId);
        when(taskRepository.existsById(taskId)).thenReturn(true);
        doNothing().when(taskAssignmentRepository).deleteByTaskIdAndAssignedUserId(taskId, userId);

        // Act
        taskAssignmentService.unassignUsersFromTask(taskId, userIds);

        // Assert
        verify(taskRepository, times(1)).existsById(taskId);
        verify(taskAssignmentRepository, times(1)).deleteByTaskIdAndAssignedUserId(taskId, userId);
    }

    @Test
    void unassignUsersFromTask_nonExistingTask_throwsException() {
        // Arrange
        List<UUID> userIds = Arrays.asList(userId);
        when(taskRepository.existsById(taskId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskAssignmentService.unassignUsersFromTask(taskId, userIds));

        verify(taskRepository, times(1)).existsById(taskId);
        verify(taskAssignmentRepository, never()).deleteByTaskIdAndAssignedUserId(any(), any());
    }

    @Test
    void updateTaskAssignment_validRequest_updatesAssignment() {
        // Arrange
        UUID assignmentId = taskAssignment.getId();
        TaskAssignmentUpdateRequest request = TaskAssignmentUpdateRequest.builder()
                .status(TaskStatus.COMPLETED)
                .userNotes("Completed successfully")
                .build();

        when(taskAssignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(taskAssignment));
        when(taskAssignmentRepository.save(any(TaskAssignment.class)))
                .thenReturn(taskAssignment);

        // Act
        TaskAssignmentResponse response = taskAssignmentService.updateTaskAssignment(assignmentId, request);

        // Assert
        assertNotNull(response);
        assertEquals(taskId, response.getTaskId());

        verify(taskAssignmentRepository, times(1)).findById(assignmentId);
        verify(taskAssignmentRepository, times(1)).save(any(TaskAssignment.class));
    }

    @Test
    void updateTaskAssignment_statusToCompleted_setsCompletionTime() {
        // Arrange
        UUID assignmentId = taskAssignment.getId();
        taskAssignment.setCompletedAt(null); // Ensure not already completed
        
        TaskAssignmentUpdateRequest request = TaskAssignmentUpdateRequest.builder()
                .status(TaskStatus.COMPLETED)
                .build();

        when(taskAssignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(taskAssignment));
        when(taskAssignmentRepository.save(any(TaskAssignment.class)))
                .thenAnswer(invocation -> {
                    TaskAssignment saved = invocation.getArgument(0);
                    assertNotNull(saved.getCompletedAt());
                    return saved;
                });

        // Act
        taskAssignmentService.updateTaskAssignment(assignmentId, request);

        // Assert
        verify(taskAssignmentRepository, times(1)).save(any(TaskAssignment.class));
    }

    @Test
    void updateTaskAssignment_statusToOngoing_setsStartedTime() {
        // Arrange
        UUID assignmentId = taskAssignment.getId();
        taskAssignment.setStatus(TaskStatus.BACKLOG);
        taskAssignment.setStartedAt(null);
        
        TaskAssignmentUpdateRequest request = TaskAssignmentUpdateRequest.builder()
                .status(TaskStatus.ONGOING)
                .build();

        when(taskAssignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(taskAssignment));
        when(taskAssignmentRepository.save(any(TaskAssignment.class)))
                .thenAnswer(invocation -> {
                    TaskAssignment saved = invocation.getArgument(0);
                    assertNotNull(saved.getStartedAt());
                    return saved;
                });

        // Act
        taskAssignmentService.updateTaskAssignment(assignmentId, request);

        // Assert
        verify(taskAssignmentRepository, times(1)).save(any(TaskAssignment.class));
    }

    @Test
    void updateTaskAssignment_nonExistingId_throwsException() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        TaskAssignmentUpdateRequest request = TaskAssignmentUpdateRequest.builder()
                .status(TaskStatus.COMPLETED)
                .build();

        when(taskAssignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskAssignmentService.updateTaskAssignment(assignmentId, request));

        verify(taskAssignmentRepository, times(1)).findById(assignmentId);
        verify(taskAssignmentRepository, never()).save(any());
    }

    @Test
    void getTaskAssignmentsByTaskId_returnsAssignments() {
        // Arrange
        when(taskAssignmentRepository.findByTaskId(taskId))
                .thenReturn(Arrays.asList(taskAssignment));

        // Act
        List<TaskAssignmentResponse> responses = taskAssignmentService.getTaskAssignmentsByTaskId(taskId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(taskId, responses.get(0).getTaskId());

        verify(taskAssignmentRepository, times(1)).findByTaskId(taskId);
    }

    @Test
    void getTaskAssignmentsByUserId_returnsAssignments() {
        // Arrange
        when(taskAssignmentRepository.findByAssignedUserId(userId))
                .thenReturn(Arrays.asList(taskAssignment));

        // Act
        List<TaskAssignmentResponse> responses = taskAssignmentService.getTaskAssignmentsByUserId(userId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(userId, responses.get(0).getAssignedUserId());

        verify(taskAssignmentRepository, times(1)).findByAssignedUserId(userId);
    }

    @Test
    void getTaskAssignmentByTaskAndUser_existingAssignment_returnsAssignment() {
        // Arrange
        when(taskAssignmentRepository.findByTaskIdAndAssignedUserId(taskId, userId))
                .thenReturn(Optional.of(taskAssignment));

        // Act
        TaskAssignmentResponse response = taskAssignmentService.getTaskAssignmentByTaskAndUser(taskId, userId);

        // Assert
        assertNotNull(response);
        assertEquals(taskId, response.getTaskId());
        assertEquals(userId, response.getAssignedUserId());

        verify(taskAssignmentRepository, times(1)).findByTaskIdAndAssignedUserId(taskId, userId);
    }

    @Test
    void getTaskAssignmentByTaskAndUser_nonExisting_throwsException() {
        // Arrange
        when(taskAssignmentRepository.findByTaskIdAndAssignedUserId(taskId, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskAssignmentService.getTaskAssignmentByTaskAndUser(taskId, userId));

        verify(taskAssignmentRepository, times(1)).findByTaskIdAndAssignedUserId(taskId, userId);
    }

    @Test
    void getTaskAssignmentsByUserAndStatus_returnsFilteredAssignments() {
        // Arrange
        when(taskAssignmentRepository.findByAssignedUserIdAndStatus(userId, TaskStatus.ONGOING))
                .thenReturn(Arrays.asList(taskAssignment));

        // Act
        List<TaskAssignmentResponse> responses = 
                taskAssignmentService.getTaskAssignmentsByUserAndStatus(userId, TaskStatus.ONGOING);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());

        verify(taskAssignmentRepository, times(1))
                .findByAssignedUserIdAndStatus(userId, TaskStatus.ONGOING);
    }

    @Test
    void getOverdueTaskAssignments_returnsOverdueAssignments() {
        // Arrange
        when(taskAssignmentRepository.findOverdueAssignments(any(Instant.class), eq(TaskStatus.COMPLETED)))
                .thenReturn(Arrays.asList(taskAssignment));

        // Act
        List<TaskAssignmentResponse> responses = taskAssignmentService.getOverdueTaskAssignments();

        // Assert
        assertNotNull(responses);
        assertFalse(responses.isEmpty());

        verify(taskAssignmentRepository, times(1))
                .findOverdueAssignments(any(Instant.class), eq(TaskStatus.COMPLETED));
    }

    @Test
    void isUserAlreadyAssigned_userAssigned_returnsTrue() {
        // Arrange
        when(taskAssignmentRepository.findByTaskIdAndAssignedUserId(taskId, userId))
                .thenReturn(Optional.of(taskAssignment));

        // Act
        boolean result = taskAssignmentService.isUserAlreadyAssigned(taskId, userId);

        // Assert
        assertTrue(result);
        verify(taskAssignmentRepository, times(1)).findByTaskIdAndAssignedUserId(taskId, userId);
    }

    @Test
    void isUserAlreadyAssigned_userNotAssigned_returnsFalse() {
        // Arrange
        when(taskAssignmentRepository.findByTaskIdAndAssignedUserId(taskId, userId))
                .thenReturn(Optional.empty());

        // Act
        boolean result = taskAssignmentService.isUserAlreadyAssigned(taskId, userId);

        // Assert
        assertFalse(result);
        verify(taskAssignmentRepository, times(1)).findByTaskIdAndAssignedUserId(taskId, userId);
    }

    @Test
    void deleteAllTaskAssignments_deletesAssignments() {
        // Arrange
        doNothing().when(taskAssignmentRepository).deleteByTaskId(taskId);

        // Act
        taskAssignmentService.deleteAllTaskAssignments(taskId);

        // Assert
        verify(taskAssignmentRepository, times(1)).deleteByTaskId(taskId);
    }
}
