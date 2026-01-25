package com.SwitchBoard.WorkspaceService.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void response_createsSuccessResponse() {
        // Act
        ApiResponse response = ApiResponse.response("Success", true);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void response_withDataAndPath_createsFullResponse() {
        // Act
        ApiResponse response = ApiResponse.response("Created", "data-object", "/api/v1/test");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Created", response.getMessage());
        assertEquals("data-object", response.getData());
        assertEquals("/api/v1/test", response.getPath());
    }

    @Test
    void error_createsErrorResponse() {
        // Act
        ApiResponse response = ApiResponse.error("Error occurred", "ERR_CODE", "/api/v1/error");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Error occurred", response.getMessage());
        assertEquals("ERR_CODE", response.getErrorCode());
        assertEquals("/api/v1/error", response.getPath());
    }

    @Test
    void constructors_workCorrectly() {
        // Test no-arg constructor
        ApiResponse resp1 = new ApiResponse();
        assertNotNull(resp1);

        // Test all-arg constructor
        ApiResponse resp2 = new ApiResponse(true, "msg", null, null, null, null);
        assertTrue(resp2.isSuccess());
        assertEquals("msg", resp2.getMessage());
    }

    @Test
    void settersAndGetters_workCorrectly() {
        // Arrange
        ApiResponse response = new ApiResponse();

        // Act
        response.setSuccess(true);
        response.setMessage("Test");
        response.setData("data");
        response.setErrorCode("CODE");
        response.setPath("/path");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Test", response.getMessage());
        assertEquals("data", response.getData());
        assertEquals("CODE", response.getErrorCode());
        assertEquals("/path", response.getPath());
    }
}
