package com.example.rezeptapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message
    ) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String msg = "validation error";
        if (ex.getBindingResult().getFieldError() != null) {
            msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        }
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        String raw = ex.getMessage() == null ? "" : ex.getMessage();
        String msg = raw.toLowerCase();

        if (msg.contains("unauthorized") || msg.contains("invalid credentials")) {
            return build(HttpStatus.UNAUTHORIZED, raw);
        }
        if (msg.contains("nicht gefunden") || msg.contains("not found")) {
            return build(HttpStatus.NOT_FOUND, raw);
        }
        return build(HttpStatus.BAD_REQUEST, raw);
    }

    // z.B. "/" oder "/favicon.ico" wenn nichts in static/ liegt
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "not found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex) {
        log.error("Unhandled exception", ex); // <-- wichtig fürs Debugging
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