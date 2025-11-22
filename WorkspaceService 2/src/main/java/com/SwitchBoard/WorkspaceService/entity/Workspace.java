package com.SwitchBoard.WorkspaceService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "workspace")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace extends BaseEntity {

    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    private WorkspaceVisibility visibility;

    // Note: This references a user in another microservice
    // No foreign key constraint should exist on this field
    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<WorkspaceAccess> workspaceAccess = new HashSet<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Assignment> assignments = new HashSet<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ActivityLog> activityLogs = new HashSet<>();

    public enum WorkspaceVisibility {
        PUBLIC, PRIVATE, ORGANIZATION_ONLY
    }
}

