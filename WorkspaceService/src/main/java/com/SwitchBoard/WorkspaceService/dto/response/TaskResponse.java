package com.SwitchBoard.WorkspaceService.dto.response;

import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private UUID id;

    private UUID assignmentId;
    private UUID assigneeUserId;
    private UUID reporterUserId;
    private String title;
    private String description;
    private TaskStatus statusKey;
    private Integer priority;
    private Integer rewardPoints;
    private Double estimatedHours;
    private String titleColor;
    private Instant deadline;
    private Instant startedAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private int orderNumber;
    private String topic;
    private Integer commentCount;
}