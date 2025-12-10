package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentRoadmapRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskRoadmapRequest;
import com.SwitchBoard.WorkspaceService.entity.Assignment;
import com.SwitchBoard.WorkspaceService.entity.Task;
import com.SwitchBoard.WorkspaceService.entity.Workspace;
import com.SwitchBoard.WorkspaceService.repository.WorkspaceRepository;
import com.SwitchBoard.WorkspaceService.service.RoadmapService;
import com.SwitchBoard.WorkspaceService.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapServiceImpl implements RoadmapService {

  private final WorkspaceRepository workspaceRepository;
  private final WorkspaceService workspaceService;

  @Value("${daily.task.deadline.hours}")
  private  double DAILY_CAPACITY_HOURS ;

    @Transactional
    @Override
    public ApiResponse addRoadmapAssignmentToWorkspace(AssignmentRoadmapRequest assignmentRoadmapRequest , UUID userId) {
        log.info("RoadmapServiceImpl :: addRoadmapAssignmentToWorkspace() :: Adding roadmap assignment to workspace: {}", assignmentRoadmapRequest);
        Assignment assignment = new Assignment();
        assignment.setTitle(assignmentRoadmapRequest.getTitle());
        assignment.setDescription(assignmentRoadmapRequest.getDescription());
        Workspace roadmapWorkspace= workspaceService.getRoadmapWorkspaceByUserId(userId);

        // Sort tasks by orderNumber to maintain sequence
        List<TaskRoadmapRequest> sortedTasks = new ArrayList<>(assignmentRoadmapRequest.getTasks());
        sortedTasks.sort(Comparator.comparingInt(TaskRoadmapRequest::getOrderNumber));
        
        // Calculate smart deadlines based on daily capacity
        List<Task> tasksWithDeadlines = calculateSmartDeadlines(sortedTasks);
        
        Set<Task> roadmapTasks = new HashSet<>(tasksWithDeadlines);
        assignment.setTasks(roadmapTasks);
        roadmapWorkspace.getAssignments().add(assignment);
        try {
            workspaceRepository.save(roadmapWorkspace);
            log.info("RoadmapServiceImpl :: addRoadmapAssignmentToWorkspace() :: Assignment saved successfully in workspace: {}", roadmapWorkspace.getName());
        } catch (Exception e) {
            log.error("RoadmapServiceImpl :: addRoadmapAssignmentToWorkspace() :: Error saving assignment to workspace: {}", e.getMessage());
            return ApiResponse.response("Error adding roadmap assignment to workspace", false);
        }
        return ApiResponse.response("Roadmap assignment added to workspace successfully" ,true);
    }

    private List<Task> calculateSmartDeadlines(List<TaskRoadmapRequest> taskRequests) {
        List<Task> tasks = new ArrayList<>();
        Instant now = Instant.now();
        ZoneId systemZone = ZoneId.systemDefault();
        
        int currentDay = 0;
        double currentDayHours = 0.0;
        boolean isFirstTask = true;
        
        for (TaskRoadmapRequest taskRequest : taskRequests) {
            Task task = new Task();
            task.setTitle(taskRequest.getTitle());
            task.setDescription(taskRequest.getDescription());
            task.setRewardPoints(taskRequest.getRewardPoints());
            task.setTitleColor(taskRequest.getTitleColor());
            task.setOrderNumber(taskRequest.getOrderNumber());
            task.setTopic(taskRequest.getTopic());
            
            // Set estimated hours (default to 1 if not provided)
            double estimatedHours = (taskRequest.getEstimatedHours() != null && taskRequest.getEstimatedHours() > 0) 
                ? taskRequest.getEstimatedHours() 
                : 1.0;
            task.setEstimatedHours(estimatedHours);
            
            // Check if adding this task exceeds daily capacity
            if (currentDayHours > 0 && (currentDayHours + estimatedHours) > DAILY_CAPACITY_HOURS) {
                // Move to next day if current day already has tasks and adding this would exceed capacity
                currentDay++;
                currentDayHours = 0.0;
            }
            
            // Add task hours to current day
            currentDayHours += estimatedHours;
            
            // Calculate deadline at midnight of the assigned day
            ZonedDateTime deadlineZoned = ZonedDateTime.now(systemZone)
                .plusDays(currentDay)
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(0);
            task.setDeadline(deadlineZoned.toInstant());
            tasks.add(task);
        }
        log.info("Smart deadline calculation complete. Total tasks: {}, Spread across {} days", 
            tasks.size(), currentDay + 1);
        
        return tasks;
    }
}
