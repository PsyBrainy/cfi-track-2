package com.wallet.alkemy.controllers;

import com.wallet.alkemy.dto.LoginRequest;
import com.wallet.alkemy.dto.LoginResponseDTO;
import com.wallet.alkemy.dto.UserDTO;
import com.wallet.alkemy.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    /** Registers a user and creates the associated bank account. */
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserDTO userDto) {
        return authService.register(userDto);
    }

    @PostMapping("/login")
    /** Authenticates a user and returns a JWT response. */
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        // Authentication failures are converted centrally by GlobalExceptionHandler.
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
