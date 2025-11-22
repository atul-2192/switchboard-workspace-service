package com.SwitchBoard.WorkspaceService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roadmap")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roadmap extends BaseEntity {

    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String domain;
    private Integer difficultyLevel;

    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean isPublic = true;

    @Column(name = "created_by")
    private UUID createdByUserId;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RoadmapStep> steps = new HashSet<>();

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Assignment> assignments = new HashSet<>();
}

