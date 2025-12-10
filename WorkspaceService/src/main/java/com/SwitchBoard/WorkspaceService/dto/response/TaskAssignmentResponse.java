package com.SwitchBoard.WorkspaceService.dto.response;

import com.SwitchBoard.WorkspaceService.entity.TaskStatus;
import com.SwitchBoard.WorkspaceService.entity.TaskAssignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignmentResponse {

    private UUID id;
    private UUID taskId;
    private String taskTitle;
    private UUID assignedUserId;
    private UUID assignedByUserId;
    
    private TaskStatus status;
    private Double spentHours;
    private Integer rewardPointsEarned;
    
    private Instant startedAt;
    private Instant completedAt;
    private Instant assignedAt;
    
    private String userNotes;
    private String submissionText;
    private String submissionUrl;
    private TaskAssignment.SubmissionStatus submissionStatus;
    private Double gradeReceived;
    private String feedback;
    
    // Audit fields
    private Instant createdAt;
    private Instant updatedAt;
    
    // Task information (optional, for detailed views)
    private TaskResponse taskDetails;
}