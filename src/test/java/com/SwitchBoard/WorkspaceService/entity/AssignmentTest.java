package com.SwitchBoard.WorkspaceService.entity;

import com.SwitchBoard.WorkspaceService.entity.enums.AssignmentType;
import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentTest {

    @Test
    void builder_createsAssignmentWithAllFields() {
        // Arrange
        Instant deadline = Instant.now().plusSeconds(86400);

        // Act
        Assignment assignment = Assignment.builder()
                .title("Assignment Title")
                .description("Description")
                .assignmentTypeKey(AssignmentType.CUSTOM)
                .totalRewardPoints(500)
                .totalEstimatedHours(25.5)
                .deadline(deadline)
                .build();

        // Assert
        assertNotNull(assignment);
        assertEquals("Assignment Title", assignment.getTitle());
        assertEquals("Description", assignment.getDescription());
        assertEquals(AssignmentType.CUSTOM, assignment.getAssignmentTypeKey());
        assertEquals(500, assignment.getTotalRewardPoints());
        assertEquals(25.5, assignment.getTotalEstimatedHours());
        assertEquals(deadline, assignment.getDeadline());
    }

    @Test
    void setters_updateFields() {
        // Arrange
        Assignment assignment = new Assignment();
        Instant deadline = Instant.now().plusSeconds(86400);

        // Act
        assignment.setTitle("Updated Title");
        assignment.setDescription("Updated Description");
        assignment.setTotalRewardPoints(100);
        assignment.setTotalEstimatedHours(10.0);
        assignment.setDeadline(deadline);

        // Assert
        assertEquals("Updated Title", assignment.getTitle());
        assertEquals("Updated Description", assignment.getDescription());
        assertEquals(100, assignment.getTotalRewardPoints());
        assertEquals(10.0, assignment.getTotalEstimatedHours());
        assertEquals(deadline, assignment.getDeadline());
    }

    @Test
    void assignmentWithTasks_handlesTasks() {
        // Arrange
        Assignment assignment = Assignment.builder()
                .title("Assignment with Tasks")
                .tasks(new HashSet<>())
                .build();

        Task task = Task.builder()
                .title("Task 1")
                .statusKey(TaskStatus.BACKLOG)
                .build();

        // Act
        assignment.getTasks().add(task);

        // Assert
        assertNotNull(assignment.getTasks());
        assertEquals(1, assignment.getTasks().size());
    }
}
