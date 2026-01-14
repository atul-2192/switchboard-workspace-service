package com.SwitchBoard.WorkspaceService.repository;

import com.SwitchBoard.WorkspaceService.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    List<Workspace> findByOwnerUserId(UUID ownerUserId);
    
    @Query("SELECT w FROM Workspace w WHERE w.name LIKE %:name%")
    List<Workspace> findByNameContainingIgnoreCase(@Param("name") String name);
    
//    @Query("SELECT w FROM Workspace w WHERE w.ownerUserId = :userId OR w.id IN " +
//           "(SELECT wa.workspace.id FROM WorkspaceAccess wa WHERE wa.userId = :userId AND wa.isActive = true)")
//    List<Workspace> findWorkspacesAccessibleByUser(@Param("userId") UUID userId);
    
//    @Query("SELECT COUNT(t) FROM Task t WHERE t.workspace.id = :workspaceId")
//    Long countTasksByWorkspaceId(@Param("workspaceId") UUID workspaceId);
    
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.workspaceId = :workspaceId")
    Long countAssignmentsByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}