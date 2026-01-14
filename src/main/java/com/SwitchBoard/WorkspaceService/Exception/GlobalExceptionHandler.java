package com.SwitchBoard.WorkspaceService.Exception;

import com.SwitchBoard.WorkspaceService.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("GlobalExceptionHandler : handleResourceNotFound : Resource not found - {} at URI: {}", ex.getMessage(), request.getRequestURI());
        ApiResponse response = ApiResponse.error(ex.getMessage(), "RESOURCE_NOT_FOUND", request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.error("GlobalExceptionHandler : handleBadRequest : Bad request - {} at URI: {}", ex.getMessage(), request.getRequestURI());
        ApiResponse response = ApiResponse.error(ex.getMessage(), "BAD_REQUEST", request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        log.error("GlobalExceptionHandler : handleUnauthorized : Unauthorized access - {} at URI: {}", ex.getMessage(), request.getRequestURI());
        ApiResponse response = ApiResponse.error(ex.getMessage(), "UNAUTHORIZED", request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<ApiResponse> handleUnexpected(UnexpectedException ex, HttpServletRequest request) {
        log.error("GlobalExceptionHandler : handleUnexpected : Unexpected error - {} at URI: {}", ex.getMessage(), request.getRequestURI());
        ApiResponse response = ApiResponse.error(ex.getMessage(), "UNEXPECTED_ERROR", request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        log.error("GlobalExceptionHandler : handleValidation : Validation failed - {} at URI: {}", message, request.getRequestURI());
        ApiResponse response = ApiResponse.error(message, "VALIDATION_ERROR", request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("GlobalExceptionHandler : handleGlobalException : Internal server error - {} at URI: {}", ex.getMessage(), request.getRequestURI(), ex);
        ApiResponse response = ApiResponse.error("Internal Server Error: " + ex.getMessage(),
                "INTERNAL_SERVER_ERROR", request.getRequestURI());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

