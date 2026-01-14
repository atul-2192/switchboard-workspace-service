package com.SwitchBoard.WorkspaceService.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "user_group_membership",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"}))
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroupMembership extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private MembershipRole role; // MEMBER, LEADER, MENTOR

    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean isActive = true;

    public enum MembershipRole {
        MEMBER, LEADER, MENTOR
    }
}

