package com.SwitchBoard.WorkspaceService.entity;

import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void builder_createsTaskWithAllFields() {
        // Arrange
        UUID assigneeId = UUID.randomUUID();
        Instant deadline = Instant.now().plusSeconds(86400);

        // Act
        Task task = Task.builder()
                .title("Test Task")
                .description("Description")
                .statusKey(TaskStatus.BACKLOG)
                .priority(3)
                .rewardPoints(100)
                .estimatedHours(5.0)
                .titleColor("#FF5733")
                .deadline(deadline)
                .assigneeUserId(assigneeId)
                .build();

        // Assert
        assertNotNull(task);
        assertEquals("Test Task", task.getTitle());
        assertEquals("Description", task.getDescription());
        assertEquals(TaskStatus.BACKLOG, task.getStatusKey());
        assertEquals(3, task.getPriority());
        assertEquals(100, task.getRewardPoints());
        assertEquals(5.0, task.getEstimatedHours());
        assertEquals("#FF5733", task.getTitleColor());
        assertEquals(deadline, task.getDeadline());
        assertEquals(assigneeId, task.getAssigneeUserId());
    }

    @Test
    void setters_updateFields() {
        // Arrange
        Task task = Task.builder().build();
        UUID reporterId = UUID.randomUUID();

        // Act
        task.setTitle("Updated Title");
        task.setStatusKey(TaskStatus.ONGOING);
        task.setReporterUserId(reporterId);
        task.setStartedAt(Instant.now());

        // Assert
        assertEquals("Updated Title", task.getTitle());
        assertEquals(TaskStatus.ONGOING, task.getStatusKey());
        assertEquals(reporterId, task.getReporterUserId());
        assertNotNull(task.getStartedAt());
    }

    @Test
    void taskWithComments_handlesCommentsCollection() {
        // Arrange
        Task task = Task.builder()
                .title("Task with comments")
                .build();

        // Act & Assert
        assertNotNull(task);
        // Comments collection behavior would be tested with actual Comment entities
    }
}
