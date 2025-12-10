package com.SwitchBoard.WorkspaceService.repository;

import com.SwitchBoard.WorkspaceService.entity.TaskAssignment;
import com.SwitchBoard.WorkspaceService.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, UUID> {

    // Find all assignments for a specific task
    List<TaskAssignment> findByTaskId(UUID taskId);
    
    // Find all assignments for a specific user
    List<TaskAssignment> findByAssignedUserId(UUID userId);
    
    // Find specific assignment for a user and task
    Optional<TaskAssignment> findByTaskIdAndAssignedUserId(UUID taskId, UUID userId);
    
    // Find assignments for a user with specific status
    List<TaskAssignment> findByAssignedUserIdAndStatus(UUID userId, TaskStatus status);

    // Find overdue assignments
    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.task.deadline < :deadline AND ta.status != :completedStatus")
    List<TaskAssignment> findOverdueAssignments(@Param("deadline") Instant deadline, @Param("completedStatus") TaskStatus completedStatus);

    // Delete all assignments for a task
    void deleteByTaskId(UUID taskId);
    
    // Delete specific assignment
    void deleteByTaskIdAndAssignedUserId(UUID taskId, UUID userId);
}