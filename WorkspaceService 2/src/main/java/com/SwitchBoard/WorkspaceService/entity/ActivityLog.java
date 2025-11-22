package com.SwitchBoard.WorkspaceService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Column(name = "user_id")
    private UUID userId;

    private String entityType; // TASK, ASSIGNMENT, WORKSPACE
    private UUID entityId;
    private String actionKey;  // TASK_MOVED, TASK_CREATED
    private String details;    // JSON string for additional context

}
