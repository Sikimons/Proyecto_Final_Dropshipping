package org.ups.dropshippingservicefinal.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ups.dropshippingservicefinal.domain.exception.InvalidDispatchDateException;
import org.ups.dropshippingservicefinal.domain.exception.InvalidRejectionReasonException;
import org.ups.dropshippingservicefinal.domain.exception.OrderAlreadyProcessedException;
import org.ups.dropshippingservicefinal.domain.exception.OrderNotFoundException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(
            OrderNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(OrderAlreadyProcessedException.class)
    public ResponseEntity<Map<String, Object>> handleOrderAlreadyProcessed(
            OrderAlreadyProcessedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidDispatchDateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDispatchDate(
            InvalidDispatchDateException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidRejectionReasonException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRejectionReason(
            InvalidRejectionReasonException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", errors, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Ocurrió un error inesperado. Por favor contacte al administrador.",
                request.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String error, String message, String path) {
        Map<String, Object> body = Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", status.value(),
                "error", error,
                "message", message,
                "path", path
        );
        return ResponseEntity.status(status).body(body);
    }
}
