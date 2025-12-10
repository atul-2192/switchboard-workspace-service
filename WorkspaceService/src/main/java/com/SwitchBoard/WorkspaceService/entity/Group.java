package com.SwitchBoard.WorkspaceService.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "group_table") // "group" is reserved keyword in SQL
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String slug;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private GroupVisibility visibility; // PUBLIC / PRIVATE

    @Column(name = "created_by")
    private UUID createdByUserId;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserGroupMembership> memberships = new HashSet<>();

    public enum GroupVisibility {
        PUBLIC, PRIVATE
    }
}
