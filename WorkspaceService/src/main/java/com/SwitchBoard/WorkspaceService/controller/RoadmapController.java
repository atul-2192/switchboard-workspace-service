package com.SwitchBoard.WorkspaceService.controller;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentCreateRequest;
import com.SwitchBoard.WorkspaceService.dto.request.AssignmentRoadmapRequest;
import com.SwitchBoard.WorkspaceService.service.RoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;


    @PostMapping("/add-assignment")
    @Operation(
            summary = "Add a roadmap to workspace and new assignment",
            description = "Adds a roadmap assignment to the specified workspace"
    )
    public ResponseEntity<ApiResponse> addRoadmapAssignmentToWorkspace(@RequestBody AssignmentRoadmapRequest assignmentRoadmapRequest,@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(roadmapService.addRoadmapAssignmentToWorkspace(assignmentRoadmapRequest , userId));
    }


}
