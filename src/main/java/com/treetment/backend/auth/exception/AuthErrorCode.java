package com.treetment.backend.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode {
    USER_NOT_FOUND("AUTH_001", "User not found", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("AUTH_002", "Invalid credentials", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH_003", "Token expired", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_EXISTS("AUTH_006", "Email already exists", HttpStatus.CONFLICT),
    NICKNAME_ALREADY_EXISTS("AUTH_007", "Nickname already exists", HttpStatus.CONFLICT),
    INVALID_TOKEN("AUTH_008", "Invalid token", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("AUTH_009", "Token not found", HttpStatus.NOT_FOUND),
    PASSWORD_RESET_TOKEN_EXPIRED("AUTH_010", "Password reset token expired", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TOKEN_ALREADY_USED("AUTH_011", "Password reset token already used", HttpStatus.BAD_REQUEST),
    USER_ALREADY_ACTIVE("AUTH_012", "User is already active", HttpStatus.BAD_REQUEST),
    USER_NOT_ACTIVE("AUTH_013", "User is not active", HttpStatus.FORBIDDEN);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    AuthErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
