package com.SwitchBoard.WorkspaceService.entity;

import com.SwitchBoard.WorkspaceService.entity.enums.WorkspaceType;
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
    private WorkspaceType workspaceType;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<WorkspaceAccess> workspaceAccess = new HashSet<>();

    @OneToMany( cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "workspaceId")
    @Builder.Default
    private Set<Assignment> assignments = new HashSet<>();



}

