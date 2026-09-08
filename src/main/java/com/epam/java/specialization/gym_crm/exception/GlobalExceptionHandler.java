package com.epam.java.specialization.gym_crm.exception;

import com.epam.java.specialization.gym_crm.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("REST response error - Validation failed. Target errors: {}", errors);
        ErrorResponseDto errorDto = ErrorResponseDto.builder()
                .timestamp(new Date())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid request payload")
                .validationErrors(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("REST response error - Entity not found: {}", ex.getMessage());
        ErrorResponseDto errorDto = createBaseErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        log.warn("REST response error - Registration conflict: {}", ex.getMessage());
        ErrorResponseDto errorDto = createBaseErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ErrorResponseDto> handleInactiveUser(InactiveUserException ex) {
        log.warn("REST response error - Access denied for inactive user: {}", ex.getMessage());
        ErrorResponseDto errorDto = createBaseErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDto);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException ex) {
        log.warn("REST response error - Authentication failed: {}", ex.getMessage());
        ErrorResponseDto errorDto = createBaseErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDto);
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ErrorResponseDto> handleUserBlocked(UserBlockedException ex) {
        log.warn("REST response error - User blocked due to brute force protection: {}", ex.getMessage());
        ErrorResponseDto errorDto = createBaseErrorResponse(HttpStatus.LOCKED, ex.getMessage());
        return ResponseEntity.status(HttpStatus.LOCKED).body(errorDto);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
        log.warn("REST response error - Access denied: {}", ex.getMessage());
        ErrorResponseDto errorDto = createBaseErrorResponse(HttpStatus.FORBIDDEN, "Access denied: You do not have permission to access this resource");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDto);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException ex) {
        log.warn("REST response error - Encountered illegal state: {}", ex.getMessage());
        ErrorResponseDto errorDto = createBaseErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        log.error("REST response error - Unexpected system error occurred: ", ex);
        ErrorResponseDto errorDto = createBaseErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        ErrorResponseDto errorDto = createBaseErrorResponse(HttpStatus.NOT_FOUND, "Resource not found: " + ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    private ErrorResponseDto createBaseErrorResponse(HttpStatus status, String message) {
        return ErrorResponseDto.builder()
                .timestamp(new Date())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
    }
}