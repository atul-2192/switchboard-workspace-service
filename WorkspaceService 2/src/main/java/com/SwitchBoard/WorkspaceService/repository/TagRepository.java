package com.SwitchBoard.WorkspaceService.repository;

import com.SwitchBoard.WorkspaceService.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByWorkspaceId(UUID workspaceId);
    
    Optional<Tag> findByWorkspaceIdAndName(UUID workspaceId, String name);
    
    boolean existsByWorkspaceIdAndName(UUID workspaceId, String name);
    
    @Query("SELECT t FROM Tag t WHERE t.workspace.id = :workspaceId AND t.name LIKE %:name%")
    List<Tag> findByWorkspaceIdAndNameContainingIgnoreCase(@Param("workspaceId") UUID workspaceId, @Param("name") String name);
    
    @Query("SELECT COUNT(task) FROM Tag tag JOIN tag.tasks task WHERE tag.id = :tagId")
    Long countTasksByTagId(@Param("tagId") UUID tagId);
}