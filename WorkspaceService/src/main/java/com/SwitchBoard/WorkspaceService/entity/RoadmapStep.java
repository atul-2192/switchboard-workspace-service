package com.SwitchBoard.WorkspaceService.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roadmap_step")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private Integer stepOrder;
    private Double estimatedHours;
    private Integer rewardPoints;

    @Column(columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isOptional = false;

    @Column(columnDefinition = "TEXT")
    private String resources; // JSON string for learning resources
}
