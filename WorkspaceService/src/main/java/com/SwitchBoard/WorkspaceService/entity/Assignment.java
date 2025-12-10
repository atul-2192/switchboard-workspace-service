package com.SwitchBoard.WorkspaceService.entity;

import com.SwitchBoard.WorkspaceService.entity.enums.AssignmentType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "assignment")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment extends BaseEntity {


    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "assignment_type_key", length = 100)
    @Enumerated(EnumType.STRING)
    private AssignmentType assignmentTypeKey; // CUSTOM / ROADMAP

    private Integer totalRewardPoints;
    private Double totalEstimatedHours;
    private Instant deadline;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "assignmentId")
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();

    @Column(name = "workspaceId", insertable = false, updatable = false)
    private UUID workspaceId;

}
