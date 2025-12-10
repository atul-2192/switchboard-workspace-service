package com.SwitchBoard.WorkspaceService.repository;

import com.SwitchBoard.WorkspaceService.entity.WorkspaceAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceAccessRepository extends JpaRepository<WorkspaceAccess, UUID> {

    // Find all access records for a workspace
    List<WorkspaceAccess> findByWorkspaceId(UUID workspaceId);
    
    // Find all access records for a user
    List<WorkspaceAccess> findByUserId(UUID userId);
    
    // Find specific access record for a user in a workspace
    Optional<WorkspaceAccess> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
    
    // Find all active access records for a workspace
    List<WorkspaceAccess> findByWorkspaceIdAndIsActiveTrue(UUID workspaceId);
    
    // Find all active access records for a user
    List<WorkspaceAccess> findByUserIdAndIsActiveTrue(UUID userId);
    
    // Find access records by access level
    List<WorkspaceAccess> findByWorkspaceIdAndAccessLevel(UUID workspaceId, WorkspaceAccess.AccessLevel accessLevel);
    
    // Check if user has access to workspace
    @Query("SELECT COUNT(wa) > 0 FROM WorkspaceAccess wa WHERE wa.workspace.id = :workspaceId AND wa.userId = :userId AND wa.isActive = true")
    boolean hasUserAccess(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);
    
    // Get user's access level for a workspace
    @Query("SELECT wa.accessLevel FROM WorkspaceAccess wa WHERE wa.workspace.id = :workspaceId AND wa.userId = :userId AND wa.isActive = true")
    Optional<WorkspaceAccess.AccessLevel> getUserAccessLevel(@Param("workspaceId") UUID workspaceId, @Param("userId") UUID userId);
    
    // Count active users with access to workspace
    @Query("SELECT COUNT(wa) FROM WorkspaceAccess wa WHERE wa.workspace.id = :workspaceId AND wa.isActive = true")
    Long countActiveUsersByWorkspaceId(@Param("workspaceId") UUID workspaceId);
    
    // Delete access record
    void deleteByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
    
    // Delete all access records for a workspace
    void deleteByWorkspaceId(UUID workspaceId);
}