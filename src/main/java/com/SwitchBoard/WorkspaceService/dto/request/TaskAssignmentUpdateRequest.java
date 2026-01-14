package com.SwitchBoard.WorkspaceService.dto.request;

import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import com.SwitchBoard.WorkspaceService.entity.TaskAssignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignmentUpdateRequest {

    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    private TaskStatus status;

    @DecimalMin(value = "0.0", message = "Spent hours must be non-negative")
    private Double spentHours;

    @Size(max = 5000, message = "User notes must not exceed 5000 characters")
    private String userNotes;

    @Size(max = 5000, message = "Submission text must not exceed 5000 characters")
    private String submissionText;

    private String submissionUrl;

    private TaskAssignment.SubmissionStatus submissionStatus;

    @DecimalMin(value = "0.0", message = "Grade must be non-negative")
    @DecimalMax(value = "100.0", message = "Grade must not exceed 100")
    private Double gradeReceived;

    @Size(max = 2000, message = "Feedback must not exceed 2000 characters")
    private String feedback;
}