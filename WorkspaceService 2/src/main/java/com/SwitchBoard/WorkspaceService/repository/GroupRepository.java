package com.SwitchBoard.WorkspaceService.repository;

import com.SwitchBoard.WorkspaceService.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    Optional<Group> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    List<Group> findByCreatedByUserId(UUID createdByUserId);
    
    List<Group> findByVisibility(Group.GroupVisibility visibility);
    
    @Query("SELECT g FROM Group g WHERE g.name LIKE %:name%")
    List<Group> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT g FROM Group g JOIN g.memberships m WHERE m.userId = :userId AND m.isActive = true")
    List<Group> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT g FROM Group g JOIN g.memberships m WHERE m.userId = :userId AND m.role = :role AND m.isActive = true")
    List<Group> findByUserIdAndRole(@Param("userId") UUID userId, @Param("role") com.SwitchBoard.WorkspaceService.entity.UserGroupMembership.MembershipRole role);
    
    @Query("SELECT COUNT(m) FROM UserGroupMembership m WHERE m.group.id = :groupId AND m.isActive = true")
    Long countMembersByGroupId(@Param("groupId") UUID groupId);
}