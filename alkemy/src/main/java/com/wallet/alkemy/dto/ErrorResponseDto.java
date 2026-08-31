package com.wallet.alkemy.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Estructura de respuesta uniforme para cualquier error de la API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public static ErrorResponseDto of(int status, String error, String message, String path) {
        return new ErrorResponseDto(Instant.now(), status, error, message, path);
    }
}
