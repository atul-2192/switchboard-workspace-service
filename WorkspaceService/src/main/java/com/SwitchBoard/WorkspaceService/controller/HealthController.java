package com.SwitchBoard.WorkspaceService.controller;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Slf4j
@Tag(name = "Health Check", description = "System health and status endpoints for monitoring service availability")
public class HealthController {

    @GetMapping
    @Operation(
        summary = "Health check endpoint",
        description = "Returns the current status and health of the workspace service. This endpoint is used by monitoring systems, load balancers, and health check services to verify that the application is running properly and can handle requests. Returns service metadata including version, environment, and current timestamp."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service is healthy and operational",
            content = @Content(schema = @Schema(implementation = Map.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service is unhealthy or experiencing issues",
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("HealthController :: healthCheck :: Performing health check");

        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "workspace-service");
        healthData.put("version", "1.0.0");
        healthData.put("timestamp", LocalDateTime.now());
        healthData.put("environment", "development");

        log.info("HealthController :: healthCheck :: Completed successfully");
        return ResponseEntity.ok(healthData);
    }

    @GetMapping("/swagger")
    @Operation(
        summary = "Swagger UI information",
        description = "Provides comprehensive information about API documentation resources including Swagger UI links, OpenAPI specification URLs, and documentation access points. This endpoint helps developers and API consumers quickly access the interactive API documentation and schema definitions."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Swagger information retrieved successfully",
            content = @Content(schema = @Schema(implementation = Map.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error while retrieving documentation links",
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> swaggerInfo() {
        log.info("HealthController :: swaggerInfo :: Providing Swagger UI information");

        Map<String, Object> swaggerData = new HashMap<>();
        swaggerData.put("swagger-ui", "http://localhost:8080/swagger-ui/index.html");
        swaggerData.put("api-docs-json", "http://localhost:8080/v3/api-docs");
        swaggerData.put("api-docs-yaml", "http://localhost:8080/v3/api-docs.yaml");
        swaggerData.put("description", "Comprehensive API documentation for the Workspace Service");

        log.info("HealthController :: swaggerInfo :: Swagger information provided successfully");
        return ResponseEntity.ok(swaggerData);
    }
}
