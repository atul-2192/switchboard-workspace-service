package com.SwitchBoard.WorkspaceService.dto.request;

import com.SwitchBoard.WorkspaceService.entity.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCreateRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 255, message = "Group name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Visibility is required")
    private Group.GroupVisibility visibility;

    @NotNull(message = "Created by user ID is required")
    private UUID createdByUserId;
}