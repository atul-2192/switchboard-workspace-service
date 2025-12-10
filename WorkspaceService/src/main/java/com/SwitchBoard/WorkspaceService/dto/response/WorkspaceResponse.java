package com.SwitchBoard.WorkspaceService.dto.response;

import com.SwitchBoard.WorkspaceService.entity.Workspace;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Workspace response payload")
public class WorkspaceResponse {

    @Schema(description = "Unique workspace identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;
    
    @Schema(description = "Workspace name", example = "Java Learning Project")
    private String name;
    
    @Schema(description = "Workspace description", example = "A comprehensive workspace for learning Java programming")
    private String description;
    
    @Schema(description = "Workspace visibility level", example = "PUBLIC")
    private Workspace.WorkspaceType workspaceType;
    
    @Schema(description = "Owner user UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID ownerUserId;
    
    @Schema(description = "List of users who have access to this workspace")
    private List<UUID> accessUserIds;
    
    @Schema(description = "Number of users with access to workspace", example = "10")
    private Integer userAccessCount;
    
    @Schema(description = "Number of tasks in workspace", example = "25")
    private Integer taskCount;
    
    @Schema(description = "Number of assignments in workspace", example = "5")
    private Integer assignmentCount;
    
    @Schema(description = "Number of tags in workspace", example = "10")
    private Integer tagCount;
    
    @Schema(description = "Workspace creation timestamp")
    private Instant createdAt;
    
    @Schema(description = "Last update timestamp")
    private Instant updatedAt;
}