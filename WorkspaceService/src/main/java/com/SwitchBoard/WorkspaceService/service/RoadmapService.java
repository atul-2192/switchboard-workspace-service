package com.SwitchBoard.WorkspaceService.service;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentRoadmapRequest;

import java.util.UUID;

public interface RoadmapService {
    public ApiResponse addRoadmapAssignmentToWorkspace(AssignmentRoadmapRequest assignmentRoadmapRequest, UUID userId);
}
