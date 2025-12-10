package com.SwitchBoard.WorkspaceService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Standard API response wrapper")
public class ApiResponse {
    
    @Schema(description = "Indicates if the operation was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Human-readable message describing the result", example = "User created successfully")
    private String message;
    
    @Schema(description = "Response data payload")
    private Object data;
    
    @Schema(description = "Error code if operation failed", example = "USER_NOT_FOUND")
    private String errorCode;
    
    @Schema(description = "Timestamp of the response", example = "2025-10-30T10:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "API endpoint path", example = "/api/v1/users")
    private String path;

    public static ApiResponse response(String message, boolean success)
    {
        return new ApiResponse(success, message, null, null, LocalDateTime.now(), null);
    }
    
    public static ApiResponse response(String message, Object data, String path) {
        return new ApiResponse(true, message, data, null, LocalDateTime.now(), path);
    }
    
    public static ApiResponse error(String message, String errorCode, String path) {
        return new ApiResponse(false, message, null, errorCode, LocalDateTime.now(), path);
    }
}
