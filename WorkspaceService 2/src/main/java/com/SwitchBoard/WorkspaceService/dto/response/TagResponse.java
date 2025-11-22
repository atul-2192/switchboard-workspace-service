package com.SwitchBoard.WorkspaceService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagResponse {

    private UUID id;
    private UUID workspaceId;
    private String name;
    private String color;
    private String description;
    private Integer taskCount;
    private Instant createdAt;
    private Instant updatedAt;
}