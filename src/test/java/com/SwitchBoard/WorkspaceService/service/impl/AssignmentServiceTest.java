package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.Exception.BadRequestException;
import com.SwitchBoard.WorkspaceService.Exception.ResourceNotFoundException;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentUpdateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskDto;
import com.SwitchBoard.WorkspaceService.dto.request.TaskUserAssignmentRequest;
import com.SwitchBoard.WorkspaceService.dto.response.AssignmentResponse;
import com.SwitchBoard.WorkspaceService.dto.response.TaskResponse;
import com.SwitchBoard.WorkspaceService.entity.Assignment;
import com.SwitchBoard.WorkspaceService.entity.Task;
import com.SwitchBoard.WorkspaceService.entity.Workspace;
import com.SwitchBoard.WorkspaceService.entity.enums.AssignmentType;
import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import com.SwitchBoard.WorkspaceService.repository.AssignmentRepository;
import com.SwitchBoard.WorkspaceService.repository.TaskRepository;
import com.SwitchBoard.WorkspaceService.repository.WorkspaceRepository;
import com.SwitchBoard.WorkspaceService.service.TaskAssignmentService;
import com.SwitchBoard.WorkspaceService.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private TaskAssignmentService taskAssignmentService;

    @InjectMocks
    private AssignmentService assignmentService;

    private UUID assignmentId;
    private UUID workspaceId;
    private UUID taskId;
    private Assignment assignment;
    private Workspace workspace;
    private Task task;

    @BeforeEach
    void setUp() {
        assignmentId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        workspace = Workspace.builder()
                .name("Test Workspace")
                .ownerUserId(UUID.randomUUID())
                .build();
        workspace.setId(workspaceId);

        assignment = Assignment.builder()
                .title("Test Assignment")
                .description("Test Description")
                .assignmentTypeKey(AssignmentType.CUSTOM)
                .totalRewardPoints(100)
                .totalEstimatedHours(10.0)
                .deadline(Instant.now().plusSeconds(86400))
                .build();
        assignment.setId(assignmentId);

        task = Task.builder()
                .title("Test Task")
                .description("Task Description")
                .statusKey(TaskStatus.BACKLOG)
                .priority(1)
                .build();
        task.setId(taskId);
    }

    @Test
    void createAssignment_validRequest_createsAssignment() {
        // Arrange
        AssignmentCreateRequest request = AssignmentCreateRequest.builder()
                .workspaceId(workspaceId)
                .title("Test Assignment")
                .description("Test Description")
                .assignmentTypeKey(AssignmentType.CUSTOM)
                .totalRewardPoints(100)
                .totalEstimatedHours(10.0)
                .deadline(Instant.now().plusSeconds(86400))
                .build();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);
        when(assignmentRepository.countTasksByAssignmentId(assignmentId)).thenReturn(0L);
        when(assignmentRepository.countCompletedTasksByAssignmentId(assignmentId)).thenReturn(0L);
        when(taskRepository.findByAssignmentId(assignmentId)).thenReturn(Collections.emptyList());

        // Act
        AssignmentResponse response = assignmentService.createAssignment(request);

        // Assert
        assertNotNull(response);
        assertEquals("Test Assignment", response.getTitle());

        verify(workspaceRepository, times(1)).findById(workspaceId);
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
        verify(workspaceRepository, times(1)).save(any(Workspace.class));
    }

    @Test
    void createAssignment_nonExistingWorkspace_throwsException() {
        // Arrange
        AssignmentCreateRequest request = AssignmentCreateRequest.builder()
                .workspaceId(workspaceId)
                .title("Test Assignment")
                .assignmentTypeKey(AssignmentType.CUSTOM)
                .build();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.createAssignment(request));

        verify(workspaceRepository, times(1)).findById(workspaceId);
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void getAssignmentById_existingId_returnsAssignment() {
        // Arrange
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.countTasksByAssignmentId(assignmentId)).thenReturn(5L);
        when(assignmentRepository.countCompletedTasksByAssignmentId(assignmentId)).thenReturn(2L);
        when(taskRepository.findByAssignmentId(assignmentId)).thenReturn(Collections.emptyList());

        // Act
        AssignmentResponse response = assignmentService.getAssignmentById(assignmentId);

        // Assert
        assertNotNull(response);
        assertEquals("Test Assignment", response.getTitle());
        assertEquals(2, response.getCompletedTasks());
        assertEquals(3, response.getPendingTasks());

        verify(assignmentRepository, times(1)).findById(assignmentId);
    }

    @Test
    void getAssignmentById_nonExistingId_throwsException() {
        // Arrange
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.getAssignmentById(assignmentId));

        verify(assignmentRepository, times(1)).findById(assignmentId);
    }

    @Test
    void getAllAssignments_withPagination_returnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Assignment> page = new PageImpl<>(Arrays.asList(assignment));

        when(assignmentRepository.findAll(pageable)).thenReturn(page);
        when(assignmentRepository.countTasksByAssignmentId(any())).thenReturn(0L);
        when(assignmentRepository.countCompletedTasksByAssignmentId(any())).thenReturn(0L);
        when(taskRepository.findByAssignmentId(any())).thenReturn(Collections.emptyList());

        // Act
        Page<AssignmentResponse> responses = assignmentService.getAllAssignments(pageable);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());

        verify(assignmentRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAssignmentsByWorkspaceId_returnsAssignments() {
        // Arrange
        when(assignmentRepository.findByWorkspaceId(workspaceId))
                .thenReturn(Arrays.asList(assignment));
        when(assignmentRepository.countTasksByAssignmentId(any())).thenReturn(0L);
        when(assignmentRepository.countCompletedTasksByAssignmentId(any())).thenReturn(0L);
        when(taskRepository.findByAssignmentId(any())).thenReturn(Collections.emptyList());

        // Act
        List<AssignmentResponse> responses = assignmentService.getAssignmentsByWorkspaceId(workspaceId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());

        verify(assignmentRepository, times(1)).findByWorkspaceId(workspaceId);
    }

    @Test
    void getOverdueAssignments_returnsOverdueAssignments() {
        // Arrange
        when(assignmentRepository.findOverdueAssignments(any(Instant.class)))
                .thenReturn(Arrays.asList(assignment));
        when(assignmentRepository.countTasksByAssignmentId(any())).thenReturn(0L);
        when(assignmentRepository.countCompletedTasksByAssignmentId(any())).thenReturn(0L);
        when(taskRepository.findByAssignmentId(any())).thenReturn(Collections.emptyList());

        // Act
        List<AssignmentResponse> responses = assignmentService.getOverdueAssignments();

        // Assert
        assertNotNull(responses);
        assertFalse(responses.isEmpty());

        verify(assignmentRepository, times(1)).findOverdueAssignments(any(Instant.class));
    }

    @Test
    void updateAssignment_validRequest_updatesAssignment() {
        // Arrange
        AssignmentUpdateRequest request = AssignmentUpdateRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .assignmentTypeKey(AssignmentType.ROADMAP)
                .totalRewardPoints(200)
                .totalEstimatedHours(20.0)
                .deadline(Instant.now().plusSeconds(172800))
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);
        when(assignmentRepository.countTasksByAssignmentId(assignmentId)).thenReturn(0L);
        when(assignmentRepository.countCompletedTasksByAssignmentId(assignmentId)).thenReturn(0L);
        when(taskRepository.findByAssignmentId(assignmentId)).thenReturn(Collections.emptyList());

        // Act
        AssignmentResponse response = assignmentService.updateAssignment(assignmentId, request);

        // Assert
        assertNotNull(response);
        verify(assignmentRepository, times(1)).findById(assignmentId);
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void updateAssignment_nonExistingId_throwsException() {
        // Arrange
        AssignmentUpdateRequest request = AssignmentUpdateRequest.builder()
                .title("Updated Title")
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.updateAssignment(assignmentId, request));

        verify(assignmentRepository, times(1)).findById(assignmentId);
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void deleteAssignment_existingId_deletesAssignment() {
        // Arrange
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(taskRepository.findByAssignmentId(assignmentId)).thenReturn(Arrays.asList(task));
        when(taskRepository.saveAll(anyList())).thenReturn(Arrays.asList(task));
        doNothing().when(assignmentRepository).delete(assignment);

        // Act
        assignmentService.deleteAssignment(assignmentId);

        // Assert
        verify(assignmentRepository, times(1)).findById(assignmentId);
        verify(assignmentRepository, times(1)).delete(assignment);
    }

    @Test
    void deleteAssignment_nonExistingId_throwsException() {
        // Arrange
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> assignmentService.deleteAssignment(assignmentId));

        verify(assignmentRepository, times(1)).findById(assignmentId);
        verify(assignmentRepository, never()).delete(any());
    }

    @Test
    void addTasksToAssignment_validRequest_addsTasks() {
        // Arrange
        TaskDto taskDto = new TaskDto();
        taskDto.setTitle("New Task");
        taskDto.setStatusKey(TaskStatus.BACKLOG);

        TaskCreateRequest request = TaskCreateRequest.builder()
                .tasks(Arrays.asList(taskDto))
                .build();

        when(taskService.createTask(request)).thenReturn(Arrays.asList(task));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);
        when(assignmentRepository.countTasksByAssignmentId(assignmentId)).thenReturn(1L);
        when(assignmentRepository.countCompletedTasksByAssignmentId(assignmentId)).thenReturn(0L);
        when(taskRepository.findByAssignmentId(assignmentId)).thenReturn(Arrays.asList(task));

        // Act
        AssignmentResponse response = assignmentService.addTasksToAssignment(assignmentId, request);

        // Assert
        assertNotNull(response);
        verify(taskService, times(1)).createTask(request);
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void removeTasksFromAssignment_validRequest_removesTasks() {
        // Arrange
        List<UUID> taskIds = Arrays.asList(taskId);

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        doNothing().when(taskRepository).deleteAllById(taskIds);
        when(assignmentRepository.countTasksByAssignmentId(assignmentId)).thenReturn(0L);
        when(assignmentRepository.countCompletedTasksByAssignmentId(assignmentId)).thenReturn(0L);
        when(taskRepository.findByAssignmentId(assignmentId)).thenReturn(Collections.emptyList());

        // Act
        AssignmentResponse response = assignmentService.removeTasksFromAssignment(assignmentId, taskIds);

        // Assert
        assertNotNull(response);
        verify(taskRepository, times(1)).deleteAllById(taskIds);
    }

    @Test
    void assignUsersToAllTasks_validRequest_assignsUsers() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        List<UUID> userIds = Arrays.asList(userId);

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(taskRepository.findByAssignmentId(assignmentId)).thenReturn(Arrays.asList(task));
        when(taskAssignmentService.assignUsersToTask(any(), any())).thenReturn(Collections.emptyList());
        when(assignmentRepository.countTasksByAssignmentId(assignmentId)).thenReturn(1L);
        when(assignmentRepository.countCompletedTasksByAssignmentId(assignmentId)).thenReturn(0L);

        // Act
        AssignmentResponse response = assignmentService.assignUsersToAllTasks(assignmentId, userIds, assignedBy);

        // Assert
        assertNotNull(response);
        verify(taskAssignmentService, times(1)).assignUsersToTask(any(), any());
    }

    @Test
    void assignUsersToAllTasks_noTasks_throwsException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID assignedBy = UUID.randomUUID();
        List<UUID> userIds = Arrays.asList(userId);

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(taskRepository.findByAssignmentId(assignmentId)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> assignmentService.assignUsersToAllTasks(assignmentId, userIds, assignedBy));

        verify(taskAssignmentService, never()).assignUsersToTask(any(), any());
    }

    @Test
    void unassignUsersFromAllTasks_validRequest_unassignsUsers() {
        // Arrange
        UUID userId = UUID.randomUUID();
        List<UUID> userIds = Arrays.asList(userId);

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(taskRepository.findByAssignmentId(assignmentId)).thenReturn(Arrays.asList(task));
        doNothing().when(taskAssignmentService).unassignUsersFromTask(any(), any());

        // Act
        assignmentService.unassignUsersFromAllTasks(assignmentId, userIds);

        // Assert
        verify(taskAssignmentService, times(1)).unassignUsersFromTask(any(), any());
    }

    @Test
    void getTasksByAssignmentId_returnsTasks() {
        // Arrange
        when(taskRepository.findByAssignmentId(assignmentId)).thenReturn(Arrays.asList(task));

        // Act
        List<TaskResponse> responses = assignmentService.getTasksByAssignmentId(assignmentId);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());

        verify(taskRepository, times(1)).findByAssignmentId(assignmentId);
    }
}
