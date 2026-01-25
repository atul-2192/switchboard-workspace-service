package com.SwitchBoard.WorkspaceService.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @InjectMocks
    private HealthController healthController;

    @Test
    void healthCheck_returnsStatus() {
        // Act
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("workspace-service", body.get("service"));
        assertEquals("1.0.0", body.get("version"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void swaggerInfo_returnsDocumentationLinks() {
        // Act
        ResponseEntity<Map<String, Object>> response = healthController.swaggerInfo();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("swagger-ui"));
        assertTrue(body.containsKey("api-docs-json"));
        assertTrue(body.containsKey("api-docs-yaml"));
    }
}
