package com.wallet.alkemy.controllers;

import com.wallet.alkemy.dto.LoginRequest;
import com.wallet.alkemy.dto.LoginResponseDto;
import com.wallet.alkemy.dto.UserDto;
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
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserDto userDto) {
        return authService.register(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequest request) {
        // BadCredentialsException (y cualquier otra excepción) es capturada
        // centralmente por GlobalExceptionHandler, que la traduce a un
        // ErrorResponseDto sanitizado con el status HTTP correspondiente.
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
