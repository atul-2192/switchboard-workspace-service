package com.SwitchBoard.WorkspaceService.dto.response;

import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskResponseTest {

    @Test
    void builder_createsTaskResponseWithAllFields() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        Instant deadline = Instant.now().plusSeconds(86400);

        // Act
        TaskResponse response = TaskResponse.builder()
                .id(taskId)
                .title("Task Title")
                .description("Description")
                .statusKey(TaskStatus.ONGOING)
                .priority(3)
                .rewardPoints(100)
                .estimatedHours(5.0)
                .titleColor("#FF5733")
                .deadline(deadline)
                .assigneeUserId(assigneeId)
                .commentCount(5)
                .build();

        // Assert
        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertEquals("Task Title", response.getTitle());
        assertEquals(TaskStatus.ONGOING, response.getStatusKey());
        assertEquals(3, response.getPriority());
        assertEquals(100, response.getRewardPoints());
        assertEquals(5.0, response.getEstimatedHours());
        assertEquals("#FF5733", response.getTitleColor());
        assertEquals(assigneeId, response.getAssigneeUserId());
        assertEquals(5, response.getCommentCount());
    }

    @Test
    void setters_updateFields() {
        // Arrange
        TaskResponse response = new TaskResponse();
        UUID taskId = UUID.randomUUID();

        // Act
        response.setId(taskId);
        response.setTitle("Updated Title");
        response.setStatusKey(TaskStatus.COMPLETED);
        response.setPriority(5);
        response.setCommentCount(10);

        // Assert
        assertEquals(taskId, response.getId());
        assertEquals("Updated Title", response.getTitle());
        assertEquals(TaskStatus.COMPLETED, response.getStatusKey());
        assertEquals(5, response.getPriority());
        assertEquals(10, response.getCommentCount());
    }

    @Test
    void noArgsConstructor_createsEmptyInstance() {
        // Act
        TaskResponse response = new TaskResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getTitle());
    }
}
