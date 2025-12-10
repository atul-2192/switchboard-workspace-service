package com.SwitchBoard.WorkspaceService.service.impl;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentRoadmapRequest;
import com.SwitchBoard.WorkspaceService.dto.request.TaskRoadmapRequest;
import com.SwitchBoard.WorkspaceService.dto.request.WorkspaceCreateRequest;
import com.SwitchBoard.WorkspaceService.entity.Assignment;
import com.SwitchBoard.WorkspaceService.entity.Task;
import com.SwitchBoard.WorkspaceService.entity.Workspace;
import com.SwitchBoard.WorkspaceService.repository.AssignmentRepository;
import com.SwitchBoard.WorkspaceService.repository.WorkspaceRepository;
import com.SwitchBoard.WorkspaceService.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapServiceImpl implements RoadmapService {

    private final AssignmentRepository assignmentRepository;
    private final WorkspaceRepository workspaceRepository;

    @Override
    public ApiResponse addRoadmapAssignmentToWorkspace(AssignmentRoadmapRequest assignmentRoadmapRequest , UUID userId) {
        log.info("RoadmapServiceImpl :: addRoadmapAssignmentToWorkspace() :: Adding roadmap assignment to workspace: {}", assignmentRoadmapRequest);
        Assignment assignment = new Assignment();
        assignment.setTitle(assignmentRoadmapRequest.getTitle());
        assignment.setDescription(assignmentRoadmapRequest.getDescription());
        Workspace roadmapWorkspace= workspaceRepository.findByOwnerUserId(userId).stream()
                .filter(workspace -> workspace.getName().equalsIgnoreCase("Roadmap Workspace"))
                .findFirst().orElse(null);

        if (roadmapWorkspace == null) {
            log.error("RoadmapServiceImpl :: addRoadmapAssignmentToWorkspace() :: 'Roadmap Workspace' not found");
            Workspace workspaceRequest = new Workspace();
            workspaceRequest.setName("Roadmap Workspace");
            workspaceRequest.setDescription("Workspace for Roadmap Assignments");
            workspaceRequest.setOwnerUserId(userId);
            roadmapWorkspace=workspaceRepository.save(workspaceRequest);
        }
        Set<Task> roadmapTasks =new HashSet<>();
        for(TaskRoadmapRequest taskRoadmapRequest : assignmentRoadmapRequest.getTasks()) {
            log.info("RoadmapServiceImpl :: addRoadmapAssignmentToWorkspace() :: Adding task to assignment: {}", taskRoadmapRequest.getTitle());
            Task task = new Task();
            task.setTitle(taskRoadmapRequest.getTitle());
            task.setDescription(taskRoadmapRequest.getDescription());
            task.setRewardPoints(taskRoadmapRequest.getRewardPoints());
            task.setTitleColor( taskRoadmapRequest.getTitleColor());
            task.setOrderNumber(taskRoadmapRequest.getOrderNumber());
            task.setTopic( taskRoadmapRequest.getTopic());
            roadmapTasks.add(task);
        }
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
}
