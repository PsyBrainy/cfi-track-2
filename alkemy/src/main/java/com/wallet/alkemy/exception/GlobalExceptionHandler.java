package com.wallet.alkemy.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wallet.alkemy.dto.ErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles controller exceptions and converts them into sanitized API responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(JwtValidationException.class)
    /** Handles invalid or expired JWTs. */
    public ResponseEntity<ErrorResponseDto> handleJwtValidation(JwtValidationException ex, HttpServletRequest request) {
        log.warn("Invalid JWT: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid or expired token", request);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    /** Handles failed authentication attempts. */
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(Exception ex, HttpServletRequest request) {
        log.warn("Authentication failure: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password", request);
    }

    /** Returns a generic response for invalid DTO fields without exposing internals. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed at endpoint [{}]: {}", request.getRequestURI(), details);
        return buildResponse(HttpStatus.BAD_REQUEST, "Required fields are missing or invalid", request);
    }

    /** Returns a generic response when the request body cannot be parsed. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Unreadable request body at endpoint [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "The request body is invalid or empty", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    /** Reports an HTTP method that is not supported by an endpoint. */
    public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method {} is not allowed at endpoint [{}]", request.getMethod(), request.getRequestURI());

        ErrorResponseDto body = ErrorResponseDto.of(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
                "HTTP method " + request.getMethod() + " is not allowed for this endpoint",
                request.getRequestURI());

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        if (ex.getSupportedMethods() != null) {
            headers.add(org.springframework.http.HttpHeaders.ALLOW, String.join(", ", ex.getSupportedMethods()));
        }
        return new ResponseEntity<>(body, headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    /** Handles invalid operation arguments. */
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request", request);
    }

    /** Handles insufficient funds without exposing internal exception details. */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientBalance(InsufficientBalanceException ex, HttpServletRequest request) {
        log.warn("Insufficient balance: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** Handles attempts to operate on a closed bank account. */
    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ErrorResponseDto> handleInactiveAccount(InactiveAccountException ex, HttpServletRequest request) {
        log.warn("Inactive bank account: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /** Returns a generic response for unexpected failures. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error while processing the request", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred. Try again later.", request);
    }

    /** Builds a consistent response body for a handled exception. */
    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponseDto body = ErrorResponseDto.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
