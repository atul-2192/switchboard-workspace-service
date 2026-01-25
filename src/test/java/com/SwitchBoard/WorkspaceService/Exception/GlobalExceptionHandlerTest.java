package com.SwitchBoard.WorkspaceService.Exception;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_returnsNotFoundResponse() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test/resource");

        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        var resp = handler.handleResourceNotFound(ex, req);

        assertEquals(404, resp.getStatusCode().value());
        ApiResponse body = resp.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("RESOURCE_NOT_FOUND", body.getErrorCode());
        assertEquals("not found", body.getMessage());
    }

    @Test
    void handleBadRequest_returnsBadRequest() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test/bad");

        BadRequestException ex = new BadRequestException("bad request");
        var resp = handler.handleBadRequest(ex, req);

        assertEquals(400, resp.getStatusCode().value());
        ApiResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals("BAD_REQUEST", body.getErrorCode());
    }

    @Test
    void handleValidation_returnsValidationError() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/test/val");

        // Create a minimal MethodArgumentNotValidException by mocking BindingResult
        org.springframework.validation.BindingResult br = mock(org.springframework.validation.BindingResult.class);
        org.springframework.validation.FieldError fe = new org.springframework.validation.FieldError("obj", "field", "must not be blank");
        when(br.getFieldError()).thenReturn(fe);

    org.springframework.web.bind.MethodArgumentNotValidException ex = mock(org.springframework.web.bind.MethodArgumentNotValidException.class);
    when(ex.getBindingResult()).thenReturn(br);

    var resp = handler.handleValidation(ex, req);

    assertEquals(400, resp.getStatusCode().value());
    ApiResponse body = resp.getBody();
    assertNotNull(body);
    assertEquals("VALIDATION_ERROR", body.getErrorCode());
    assertEquals("must not be blank", body.getMessage());
    }
}
