package com.SwitchBoard.WorkspaceService.entity;

import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "task_assignment", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "assigned_user_id"}))
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "assigned_user_id", nullable = false)
    private UUID assignedUserId;

    @Column(name = "assigned_by_user_id")
    private UUID assignedByUserId; // Who assigned this task to the user

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.ONGOING;
    
    private Integer rewardPointsEarned; // Points earned by this specific user
    
    private Instant startedAt;
    private Instant completedAt;
    private Instant assignedAt;
    
    @Column(columnDefinition = "TEXT")
    private String userNotes; // Personal notes for this user

    // Individual submission/completion data
    @Column(columnDefinition = "TEXT")
    private String submissionText;
    
    private String submissionUrl; // Link to submitted work
    
    @Enumerated(EnumType.STRING)
    private SubmissionStatus submissionStatus;
    
    private Double gradeReceived; // If task is graded
    
    @Column(columnDefinition = "TEXT")
    private String feedback; // Feedback from instructor/reviewer

    public enum SubmissionStatus {
        NOT_SUBMITTED,
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        NEEDS_REVISION
    }
}