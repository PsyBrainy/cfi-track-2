package com.wallet.alkemy.exception;

/**
 * Excepción de dominio que representa un token JWT inválido, expirado o mal formado.
 * Se lanza en lugar de RuntimeException para poder distinguirla y mapearla a una
 * respuesta HTTP controlada (401) sin filtrar detalles internos al cliente.
 */
public class JwtValidationException extends RuntimeException {

    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtValidationException(String message) {
        super(message);
    }
}
