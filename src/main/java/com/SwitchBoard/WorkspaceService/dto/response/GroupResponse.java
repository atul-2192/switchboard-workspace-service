package com.SwitchBoard.WorkspaceService.dto.response;

import com.SwitchBoard.WorkspaceService.entity.Group;
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
public class GroupResponse {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private Group.GroupVisibility visibility;
    private UUID createdByUserId;
    private Integer memberCount;
    private Instant createdAt;
    private Instant updatedAt;
}