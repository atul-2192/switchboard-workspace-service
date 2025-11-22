package com.SwitchBoard.WorkspaceService.dto.response;

import com.SwitchBoard.WorkspaceService.entity.TaskStatus;
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
    private UUID workspaceId;
    private UUID assignmentId;
    private UUID parentTaskId;
    private UUID assigneeUserId;
    private UUID reporterUserId;
    private String title;
    private String description;
    private String taskTypeKey;
    private TaskStatus statusKey;
    private Integer priority;
    private Integer rewardPoints;
    private Double estimatedHours;
    private Double spentHours;
    private String titleColor;
    private Double position;
    private Instant deadline;
    private Instant startedAt;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<TagResponse> tags;
    private Integer subTaskCount;
    private Integer commentCount;
}