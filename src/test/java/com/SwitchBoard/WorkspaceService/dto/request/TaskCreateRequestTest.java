package com.SwitchBoard.WorkspaceService.dto.request;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskCreateRequestTest {

    @Test
    void builder_createsRequest() {
        // Arrange
        UUID assigneeId = UUID.randomUUID();
        UUID reporterId = UUID.randomUUID();
        TaskDto dto = new TaskDto();
        dto.setTitle("Task");

        // Act
        TaskCreateRequest request = TaskCreateRequest.builder()
                .assigneeUserId(assigneeId)
                .reporterUserId(reporterId)
                .tasks(List.of(dto))
                .build();

        // Assert
        assertNotNull(request);
        assertEquals(assigneeId, request.getAssigneeUserId());
        assertEquals(reporterId, request.getReporterUserId());
        assertEquals(1, request.getTasks().size());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        // Arrange
        TaskCreateRequest request = new TaskCreateRequest();
        UUID assigneeId = UUID.randomUUID();
        TaskDto dto = new TaskDto();

        // Act
        request.setAssigneeUserId(assigneeId);
        request.setTasks(List.of(dto));

        // Assert
        assertEquals(assigneeId, request.getAssigneeUserId());
        assertEquals(1, request.getTasks().size());
    }
}
