package com.SwitchBoard.WorkspaceService.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCreateRequest {

    private UUID assigneeUserId;

    private UUID reporterUserId;

    private List<TaskDto> tasks;
}