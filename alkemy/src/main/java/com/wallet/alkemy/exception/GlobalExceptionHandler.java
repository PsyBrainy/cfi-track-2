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
        log.warn("JWT inválido: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Token inválido o expirado", request);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    /** Handles failed authentication attempts. */
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(Exception ex, HttpServletRequest request) {
        log.warn("Fallo de autenticación: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Email o contraseña inválidos", request);
    }

    /** Returns a generic response for invalid DTO fields without exposing internals. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validación fallida en endpoint [{}]: {}", request.getRequestURI(), details);
        return buildResponse(HttpStatus.BAD_REQUEST, "Faltan campos obligatorios o los datos ingresados son inválidos", request);
    }

    /** Returns a generic response when the request body cannot be parsed. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Cuerpo de solicitud ilegible en endpoint [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es inválido o se encuentra vacío", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    /** Reports an HTTP method that is not supported by an endpoint. */
    public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("El método {} no está permitido en el endpoint [{}]", request.getMethod(), request.getRequestURI());

        ErrorResponseDto body = ErrorResponseDto.of(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
                "El método HTTP " + request.getMethod() + " no está permitido para este endpoint",
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
        log.warn("Solicitud inválida: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** Handles insufficient funds without exposing internal exception details. */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientBalance(InsufficientBalanceException ex, HttpServletRequest request) {
        log.warn("Saldo insuficiente: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** Handles attempts to operate on a closed bank account. */
    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ErrorResponseDto> handleInactiveAccount(InactiveAccountException ex, HttpServletRequest request) {
        log.warn("Cuenta bancaria inactiva: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("Usuario no encontrado: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest request) {
        log.warn("Cuenta no encontrada: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** Returns a generic response for unexpected failures. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado procesando la solicitud", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno. Intente nuevamente más tarde.", request);
    }

    /** Builds a consistent response body for a handled exception. */
    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponseDto body = ErrorResponseDto.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
