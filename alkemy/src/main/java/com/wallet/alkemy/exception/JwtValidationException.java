package com.wallet.alkemy.exception;

/**
 * Domain exception for invalid, expired, or malformed JWTs.
 * It lets the application map token failures to a controlled 401 response.
 */
public class JwtValidationException extends RuntimeException {

    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtValidationException(String message) {
        super(message);
    }
}
