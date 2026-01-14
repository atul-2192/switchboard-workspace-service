package com.SwitchBoard.WorkspaceService.entity;

import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "task")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {


    @Column(name = "assignee_user_id")
    private UUID assigneeUserId;

    @Column(name = "reporter_user_id")
    private UUID reporterUserId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;


    @Enumerated(EnumType.STRING)
    private TaskStatus statusKey;    // BACKLOG, ONGOING, COMPLETED

    private Integer priority;
    private Integer rewardPoints;
    private Double estimatedHours;
    private String titleColor;
    private int orderNumber;
    private String topic;
    private Instant deadline;
    private Instant startedAt;
    private Instant completedAt;


    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();

    @Column(name = "assignmentId", insertable = false, updatable = false)
    private UUID assignmentId;

}
