package com.SwitchBoard.WorkspaceService.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRoadmapRequest {

    private UUID id;
    private String title;
    private String description;
    private int rewardPoints;
    private String titleColor;
    private int daysToComplete;
    private UUID assignmentId;
    private String topic;
    private int orderNumber;
    private Double estimatedHours;
}
