package com.treetment.backend.global.exception;

import com.treetment.backend.auth.exception.AuthException;
import com.treetment.backend.global.dto.response.ApiResponse;
import com.treetment.backend.global.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAuthException(AuthException e, HttpServletRequest request) {
        log.error("AuthException occurred: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();
        
        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error(e.getMessage(), errorResponse);
        return ResponseEntity.status(e.getHttpStatus()).body(apiResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("Validation failed: {}", e.getMessage(), e);
        
        Map<String, Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            details.put(error.getField(), error.getDefaultMessage());
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed")
                .details(details)
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();
        
        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error("Validation failed", errorResponse);
        return ResponseEntity.badRequest().body(apiResponse);
    }
    
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBindException(BindException e, HttpServletRequest request) {
        log.error("BindException occurred: {}", e.getMessage(), e);
        
        Map<String, Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            details.put(error.getField(), error.getDefaultMessage());
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("BIND_ERROR")
                .message("Binding failed")
                .details(details)
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();
        
        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error("Binding failed", errorResponse);
        return ResponseEntity.badRequest().body(apiResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.error("IllegalArgumentException occurred: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("ILLEGAL_ARGUMENT")
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();
        
        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error(e.getMessage(), errorResponse);
        return ResponseEntity.badRequest().body(apiResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGlobalException(Exception e, HttpServletRequest request) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .build();
        
        ApiResponse<ErrorResponse> apiResponse = ApiResponse.error("Internal server error", errorResponse);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }
}
