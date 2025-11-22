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
    
    // Find assignments by status
    List<TaskAssignment> findByStatus(TaskStatus status);
    
    // Find assignments for a user with specific status
    List<TaskAssignment> findByAssignedUserIdAndStatus(UUID userId, TaskStatus status);
    
    // Find assignments for a task with specific status
    List<TaskAssignment> findByTaskIdAndStatus(UUID taskId, TaskStatus status);
    
    // Find assignments by who assigned them
    List<TaskAssignment> findByAssignedByUserId(UUID assignedByUserId);
    
    // Find overdue assignments
    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.task.deadline < :deadline AND ta.status != :completedStatus")
    List<TaskAssignment> findOverdueAssignments(@Param("deadline") Instant deadline, @Param("completedStatus") TaskStatus completedStatus);
    
    // Find assignments for a user in a specific workspace
    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.assignedUserId = :userId AND ta.task.workspace.id = :workspaceId")
    List<TaskAssignment> findByAssignedUserIdAndWorkspaceId(@Param("userId") UUID userId, @Param("workspaceId") UUID workspaceId);
    
    // Find assignments for tasks in an assignment
    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.task.assignment.id = :assignmentId")
    List<TaskAssignment> findByAssignmentId(@Param("assignmentId") UUID assignmentId);
    
    // Count assignments for a task
    @Query("SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.task.id = :taskId")
    Long countByTaskId(@Param("taskId") UUID taskId);
    
    // Count completed assignments for a task
    @Query("SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.task.id = :taskId AND ta.status = :completedStatus")
    Long countCompletedByTaskId(@Param("taskId") UUID taskId, @Param("completedStatus") TaskStatus completedStatus);
    
    // Count assignments for a user
    @Query("SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.assignedUserId = :userId")
    Long countByAssignedUserId(@Param("userId") UUID userId);
    
    // Count completed assignments for a user
    @Query("SELECT COUNT(ta) FROM TaskAssignment ta WHERE ta.assignedUserId = :userId AND ta.status = :completedStatus")
    Long countCompletedByAssignedUserId(@Param("userId") UUID userId, @Param("completedStatus") TaskStatus completedStatus);
    
    // Calculate total spent hours for a user
    @Query("SELECT COALESCE(SUM(ta.spentHours), 0) FROM TaskAssignment ta WHERE ta.assignedUserId = :userId")
    Double getTotalSpentHoursByUserId(@Param("userId") UUID userId);
    
    // Calculate total reward points earned by a user
    @Query("SELECT COALESCE(SUM(ta.rewardPointsEarned), 0) FROM TaskAssignment ta WHERE ta.assignedUserId = :userId AND ta.status = :completedStatus")
    Integer getTotalRewardPointsByUserId(@Param("userId") UUID userId, @Param("completedStatus") TaskStatus completedStatus);
    
    // Delete all assignments for a task
    void deleteByTaskId(UUID taskId);
    
    // Delete specific assignment
    void deleteByTaskIdAndAssignedUserId(UUID taskId, UUID userId);
}