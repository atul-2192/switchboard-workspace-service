package com.SwitchBoard.WorkspaceService.dto.response;

import com.SwitchBoard.WorkspaceService.entity.AssignmentType;
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
public class AssignmentResponse {

    private UUID id;
    private UUID workspaceId;
    private String title;
    private String description;
    private AssignmentType assignmentTypeKey;
    private Integer totalRewardPoints;
    private Double totalEstimatedHours;
    private Instant deadline;
    private UUID roadmapId;
    
    // Assignment statistics
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer pendingTasks;
    private Double completionPercentage;
    private Double totalSpentHours;
    
    // Audit fields
    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
    
    // Associated tasks (optional, can be loaded separately for performance)
    private List<TaskResponse> tasks;
}