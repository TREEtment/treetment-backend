package com.treetment.backend.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.treetment.backend.global.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ResponseUtil {
    
    private final ObjectMapper objectMapper;
    
    public void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        sendSuccessResponse(response, message, null);
    }
    
    public void sendSuccessResponse(HttpServletResponse response, String message, Object data) throws IOException {
        ApiResponse<Object> apiResponse = ApiResponse.success(message, data);
        sendResponse(response, HttpStatus.OK, apiResponse);
    }
    
    public void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        sendErrorResponse(response, status, message, null);
    }
    
    public void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message, Object data) throws IOException {
        ApiResponse<Object> apiResponse = ApiResponse.error(message, data);
        sendResponse(response, status, apiResponse);
    }
    
    private void sendResponse(HttpServletResponse response, HttpStatus status, ApiResponse<Object> apiResponse) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }
}
