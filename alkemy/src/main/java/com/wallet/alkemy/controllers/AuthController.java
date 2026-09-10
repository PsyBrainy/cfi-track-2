package com.wallet.alkemy.controllers;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wallet.alkemy.dto.LoginRequest;
import com.wallet.alkemy.dto.LoginResponseDTO;
import com.wallet.alkemy.dto.UserDTO;
import com.wallet.alkemy.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    /**
     * Registers a user and creates the associated bank account.
     */
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserDTO userDto) {
        return authService.register(userDto);
    }

    @GetMapping("/check-session")
    public ResponseEntity<Void> checkSession() {
        // Si la petición llega hasta acá, el filtro JwtFilter ya validó el token
        return ResponseEntity.ok().build();
    }

@GetMapping("/activate")
/** Activa la cuenta del usuario y gestiona la redirección física en el protocolo HTTP */
public ResponseEntity<Void> activateAccount(@RequestParam("token") String token) {
    HttpHeaders headers = new HttpHeaders();

    try {
        // Ejecutamos la lógica de negocio pura en el servicio
        boolean cuentaActivadaAhora = authService.activateAccount(token);

        if (cuentaActivadaAhora) {
            // ÉXITO PRIMARIO: Cuenta activada por primera vez
            headers.setLocation(URI.create("http://127.0.0.1:5500/login.html?activado=exito"));
        } else {
            // ÉXITO SECUNDARIO: La cuenta ya se encontraba activa previamente
            headers.setLocation(URI.create("http://127.0.0.1:5500/login.html?activado=ya_activado"));
        }

    } catch (Exception e) {
        // FALLO CONTROLADO: El token expiró o la firma digital es inválida
        headers.setLocation(URI.create("http://127.0.0.1:5500/login.html?activado=error"));
    }

    // Enviamos el código de redirección HTTP 302 estándar
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
}


    @PostMapping("/login")
    /**
     * Authenticates a user and returns a JWT response.
     */
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequest request) {
        // Authentication failures are converted centrally by GlobalExceptionHandler.
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

}
