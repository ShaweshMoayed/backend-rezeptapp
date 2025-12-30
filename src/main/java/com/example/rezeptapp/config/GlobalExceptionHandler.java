package com.example.rezeptapp.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message
    ) {}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();

        if (msg.contains("unauthorized")) return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
        if (msg.contains("invalid credentials")) return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
        if (msg.contains("nicht gefunden") || msg.contains("not found")) return build(HttpStatus.NOT_FOUND, ex.getMessage());

        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error");
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
        return ResponseEntity.status(status).body(body);
    }
}