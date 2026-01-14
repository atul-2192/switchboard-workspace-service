package com.SwitchBoard.WorkspaceService.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentTaskManagementRequest {

    @NotEmpty(message = "Task IDs list cannot be empty")
    private List<@NotNull(message = "Task ID cannot be null") UUID> taskIds;
}