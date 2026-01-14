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
public class TaskUserAssignmentRequest {

    @NotEmpty(message = "User IDs list cannot be empty")
    private List<@NotNull(message = "User ID cannot be null") UUID> userIds;

    private UUID assignedBy; // Optional: who is assigning these users

    @Size(max = 1000, message = "Assignment notes must not exceed 1000 characters")
    private String assignmentNotes; // Optional notes for the assignment
}