package com.SwitchBoard.WorkspaceService.repository;

import com.SwitchBoard.WorkspaceService.entity.Assignment;
import com.SwitchBoard.WorkspaceService.entity.AssignmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    List<Assignment> findByWorkspaceId(UUID workspaceId);
    
    List<Assignment> findByAssignmentTypeKey(AssignmentType assignmentTypeKey);
    
    List<Assignment> findByRoadmapId(UUID roadmapId);
    
    @Query("SELECT a FROM Assignment a WHERE a.workspace.id = :workspaceId AND a.title LIKE %:title%")
    List<Assignment> findByWorkspaceIdAndTitleContainingIgnoreCase(@Param("workspaceId") UUID workspaceId, @Param("title") String title);
    
    @Query("SELECT a FROM Assignment a WHERE a.deadline < :deadline")
    List<Assignment> findOverdueAssignments(@Param("deadline") Instant deadline);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignment.id = :assignmentId")
    Long countTasksByAssignmentId(@Param("assignmentId") UUID assignmentId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignment.id = :assignmentId AND t.statusKey = 'COMPLETED'")
    Long countCompletedTasksByAssignmentId(@Param("assignmentId") UUID assignmentId);
}