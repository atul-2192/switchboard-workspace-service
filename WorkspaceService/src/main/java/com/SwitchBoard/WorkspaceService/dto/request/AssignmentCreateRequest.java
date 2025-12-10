package com.SwitchBoard.WorkspaceService.dto.request;

import com.SwitchBoard.WorkspaceService.entity.enums.AssignmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentCreateRequest {

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    @NotBlank(message = "Assignment title is required")
    @Size(max = 255, message = "Assignment title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Assignment type is required")
    private AssignmentType assignmentTypeKey;

    @Min(value = 0, message = "Total reward points must be non-negative")
    private Integer totalRewardPoints;

    @DecimalMin(value = "0.0", message = "Total estimated hours must be non-negative")
    private Double totalEstimatedHours;

    private Instant deadline;
    private TaskCreateRequest newTasks;
}