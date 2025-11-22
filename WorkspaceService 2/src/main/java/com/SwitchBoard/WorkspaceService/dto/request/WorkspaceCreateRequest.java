package com.SwitchBoard.WorkspaceService.dto.request;

import com.SwitchBoard.WorkspaceService.entity.Workspace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceCreateRequest {

    @NotBlank(message = "Workspace name is required")
    @Size(max = 255, message = "Workspace name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Visibility is required")
    private Workspace.WorkspaceVisibility visibility;


    private UUID ownerUserId;

    @Schema(description = "List of user IDs who will have READ access to the workspace")
    @Builder.Default
    private List<UUID> readAccessUserIds = new ArrayList<>();
    
    @Schema(description = "List of user IDs who will have WRITE access to the workspace")
    @Builder.Default
    private List<UUID> writeAccessUserIds = new ArrayList<>();
    
    @Schema(description = "List of user IDs who will have ADMIN access to the workspace")
    @Builder.Default
    private List<UUID> adminAccessUserIds = new ArrayList<>();

    /**
     * Validates that no user appears in multiple access lists
     * @return list of validation errors, empty if valid
     */
    public List<String> validateUserAccess() {
        List<String> errors = new ArrayList<>();
        Set<UUID> allUsers = new HashSet<>();
        
        // Check for duplicates in read access
        for (UUID userId : readAccessUserIds) {
            if (!allUsers.add(userId)) {
                errors.add("User " + userId + " appears in multiple access lists");
            }
        }
        
        // Check for duplicates in write access
        for (UUID userId : writeAccessUserIds) {
            if (!allUsers.add(userId)) {
                errors.add("User " + userId + " appears in multiple access lists");
            }
        }
        
        // Check for duplicates in admin access
        for (UUID userId : adminAccessUserIds) {
            if (!allUsers.add(userId)) {
                errors.add("User " + userId + " appears in multiple access lists");
            }
        }
        
        return errors;
    }
}
