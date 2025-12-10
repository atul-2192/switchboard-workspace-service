package com.SwitchBoard.WorkspaceService.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentRoadmapRequest {

    @NotBlank(message = "Assignment title is required")
    @Size(max = 255, message = "Assignment title must not exceed 255 characters")
    private String title;
    @NotBlank(message = "Assignment description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private List<TaskRoadmapRequest> tasks;
}