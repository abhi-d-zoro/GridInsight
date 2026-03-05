package com.gridinsight.backend.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(new ApiError("VALIDATION_FAILED", errors));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException ex) {
        return ResponseEntity.status(409)
                .body(new ApiError("CONFLICT", List.of("Data conflict")));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(409).body(new ApiError("CONFLICT", List.of(ex.getMessage())));
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<?> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(404).body(new ApiError("NOT_FOUND", List.of(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnknown(Exception ex) {
        return ResponseEntity.internalServerError().body(new ApiError("INTERNAL_ERROR", List.of("Unexpected error")));
    }

    @ExceptionHandler(com.gridinsight.backend.exception.UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(com.gridinsight.backend.exception.UnauthorizedException ex) {
        return ResponseEntity.status(401).body(new ApiError("UNAUTHORIZED", List.of(ex.getMessage())));
    }

    @ExceptionHandler(com.gridinsight.backend.exception.AccountLockedException.class)
    public ResponseEntity<?> handleLocked(com.gridinsight.backend.exception.AccountLockedException ex) {
        return ResponseEntity.status(423).body(new ApiError("LOCKED", List.of(ex.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ApiError("BAD_REQUEST", List.of(ex.getMessage())));
    }

    record ApiError(String code, List<String> messages) {}
}