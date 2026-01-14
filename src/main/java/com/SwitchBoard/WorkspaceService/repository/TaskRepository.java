package com.SwitchBoard.WorkspaceService.repository;

import com.SwitchBoard.WorkspaceService.entity.Task;
import com.SwitchBoard.WorkspaceService.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

//    List<Task> findByWorkspaceId(UUID workspaceId);
    
    List<Task> findByAssignmentId(UUID assignmentId);
    
    List<Task> findByAssigneeUserId(UUID assigneeId);
    
    List<Task> findByReporterUserId(UUID reporterId);
    
//    List<Task> findByParentTaskId(UUID parentTaskId);
    
    List<Task> findByStatusKey(TaskStatus statusKey);
    
//    List<Task> findByWorkspaceIdAndStatusKey(UUID workspaceId, TaskStatus statusKey);
    
//    @Query("SELECT t FROM Task t WHERE t.workspace.id = :workspaceId AND t.parentTask IS NULL ORDER BY t.position ASC")
//    List<Task> findRootTasksByWorkspaceId(@Param("workspaceId") UUID workspaceId);
//
//    @Query("SELECT t FROM Task t WHERE t.assigneeUserId = :assigneeId AND t.statusKey IN :statuses")
//    List<Task> findByAssigneeIdAndStatusKeyIn(@Param("assigneeId") UUID assigneeId, @Param("statuses") List<TaskStatus> statuses);
//
    @Query("SELECT t FROM Task t WHERE t.deadline < :deadline AND t.statusKey != :completedStatus")
    List<Task> findOverdueTasks(@Param("deadline") Instant deadline, @Param("completedStatus") TaskStatus completedStatus);
    
//    @Query("SELECT t FROM Task t WHERE t.workspace.id = :workspaceId AND t.title LIKE %:title%")
//    List<Task> findByWorkspaceIdAndTitleContainingIgnoreCase(@Param("workspaceId") UUID workspaceId, @Param("title") String title);
//
//    @Query("SELECT COUNT(t) FROM Task t WHERE t.parentTask.id = :parentTaskId")
//    Long countSubTasksByParentTaskId(@Param("parentTaskId") UUID parentTaskId);
//
//    @Query("SELECT SUM(t.rewardPoints) FROM Task t WHERE t.assigneeUserId = :userId AND t.statusKey = :completedStatus")
//    Integer getTotalRewardPointsByUserId(@Param("userId") UUID userId, @Param("completedStatus") TaskStatus completedStatus);
}