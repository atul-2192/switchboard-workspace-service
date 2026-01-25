package com.SwitchBoard.WorkspaceService.dto.response;

import com.SwitchBoard.WorkspaceService.entity.enums.AssignmentType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentResponseTest {

    @Test
    void builder_createsAssignmentResponseWithAllFields() {
        // Arrange
        UUID assignmentId = UUID.randomUUID();
        Instant deadline = Instant.now().plusSeconds(86400);

        // Act
        AssignmentResponse response = AssignmentResponse.builder()
                .id(assignmentId)
                .title("Assignment Title")
                .description("Description")
                .assignmentTypeKey(AssignmentType.ROADMAP)
                .totalRewardPoints(500)
                .totalEstimatedHours(40.0)
                .deadline(deadline)
                .totalTasks(15)
                .completedTasks(10)
                .build();

        // Assert
        assertNotNull(response);
        assertEquals(assignmentId, response.getId());
        assertEquals("Assignment Title", response.getTitle());
        assertEquals("Description", response.getDescription());
        assertEquals(AssignmentType.ROADMAP, response.getAssignmentTypeKey());
        assertEquals(500, response.getTotalRewardPoints());
        assertEquals(40.0, response.getTotalEstimatedHours());
        assertEquals(deadline, response.getDeadline());
        assertEquals(15, response.getTotalTasks());
        assertEquals(10, response.getCompletedTasks());
    }

    @Test
    void setters_updateFields() {
        // Arrange
        AssignmentResponse response = new AssignmentResponse();
        UUID assignmentId = UUID.randomUUID();

        // Act
        response.setId(assignmentId);
        response.setTitle("Updated Title");
        response.setTotalRewardPoints(1000);
        response.setTotalTasks(20);
        response.setCompletedTasks(15);

        // Assert
        assertEquals(assignmentId, response.getId());
        assertEquals("Updated Title", response.getTitle());
        assertEquals(1000, response.getTotalRewardPoints());
        assertEquals(20, response.getTotalTasks());
        assertEquals(15, response.getCompletedTasks());
    }

    @Test
    void noArgsConstructor_createsEmptyInstance() {
        // Act
        AssignmentResponse response = new AssignmentResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getTitle());
    }
}
