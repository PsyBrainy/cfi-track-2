package com.wallet.alkemy.controllers;

import java.util.Map;

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
    /**
     * Activa la cuenta de un usuario utilizando el token enviado por correo.
     */
    public ResponseEntity<Map<String, Object>> activateAccount(@RequestParam("token") String token) {
        return authService.activateAccount(token);
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
