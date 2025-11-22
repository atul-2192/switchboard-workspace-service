package com.SwitchBoard.WorkspaceService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "assignment")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id")
    private Roadmap roadmap; // If assignment is based on a roadmap

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();
}
