package com.SwitchBoard.WorkspaceService.dto.request;

import com.SwitchBoard.WorkspaceService.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCreateRequest {

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    private UUID assignmentId;
    private UUID parentTaskId;
    private UUID assigneeUserId;
    private UUID reporterUserId;

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Task title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private String taskTypeKey;
    private TaskStatus statusKey;

    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 5, message = "Priority must not exceed 5")
    private Integer priority;

    @Min(value = 0, message = "Reward points must be non-negative")
    private Integer rewardPoints;

    @DecimalMin(value = "0.0", message = "Estimated hours must be non-negative")
    private Double estimatedHours;

    @DecimalMin(value = "0.0", message = "Spent hours must be non-negative")
    private Double spentHours;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Title color must be a valid hex color")
    private String titleColor;

    private Double position;
    private Instant deadline;
    private Set<UUID> tagIds;
}