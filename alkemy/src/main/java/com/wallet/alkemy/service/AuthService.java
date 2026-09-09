package com.wallet.alkemy.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wallet.alkemy.config.tableUserRole;
import com.wallet.alkemy.dto.LoginRequest;
import com.wallet.alkemy.dto.LoginResponseDTO;
import com.wallet.alkemy.dto.UserDTO;
import com.wallet.alkemy.exception.JwtValidationException;
import com.wallet.alkemy.models.tableUser;
import com.wallet.alkemy.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BankAccountService bankAccountService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Map<String, Object>> register(UserDTO userDto) {
        Map<String, Object> response = new HashMap<>();

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            response.put("status", "error");
            response.put("message", "El correo electrónico ya está registrado.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(userDto.getBirthDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El formato de la fecha de nacimiento es inválido. Use AAAA-MM-DD.");
        }

        tableUser user = new tableUser();
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setBirthDate(birthDate);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setAddress(userDto.getAddress());
        user.setDni(userDto.getDni());
        user.setCity(userDto.getCity());
        user.setProvince(userDto.getProvince());
        user.setCountry("Argentina");
        user.setPostalCode(userDto.getPostalCode());
        user.setGender(userDto.getGender());
        user.setEmployment(userDto.getEmployment());
        user.setActive(false);
        user.setDateCreated(LocalDate.now());
        user.setLastLogin(LocalDate.now());
        user.setRole(tableUserRole.USER);

        userRepository.save(user);
        bankAccountService.createBankAccount(user);
        // 1. Generamos el Token de Activación único
        String activationToken = jwtService.generateActivationToken(user.getEmail());

        // 2. Enviamos el correo 
        try {
            emailService.sendActivationEmail(user.getEmail(), user.getName(), activationToken);
        } catch (Exception e) {
            // Esto provocará el Rollback de la base de datos si el correo falla
            throw new RuntimeException("Error al enviar el email de activación. Registro cancelado.", e);
        }

        response.put("status", "success");
        response.put("message", "Usuario registrado. Por favor, verifica tu correo electrónico para activar la cuenta.");
        response.put("data", Map.of("email", user.getEmail(), "activationToken", activationToken));

        return ResponseEntity.ok(response);
    }
    /**
     * Valida el token de activación y cambia el estado del usuario a activo.
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Map<String, Object>> activateAccount(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Extraer el email del token (Valida automáticamente expiración y firma)
            String email = jwtService.getUsername(token);

            // 2. Buscar al usuario
            tableUser user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado asociado a este token."));

            // 3. Validar si ya estaba activo
            if (user.isActive()) {
                response.put("status", "info");
                response.put("message", "Esta cuenta ya se encuentra activada.");
                return ResponseEntity.ok(response);
            }

            // 4. Cambiar el estado a activo
            user.setActive(true);
            userRepository.save(user);

            response.put("status", "success");
            response.put("message", "¡Cuenta activada con éxito! Ya puedes iniciar sesión.");
            return ResponseEntity.ok(response);

        } catch (JwtValidationException e) {
            response.put("status", "error");
            response.put("message", "El enlace de activación es inválido o ha expirado.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    /**
     * Authenticates a user and returns a signed JWT.
     */
public LoginResponseDTO login(LoginRequest request) {
    // 1. Validamos la autenticación de Spring Security primero en un bloque controlado
    try {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Si la contraseña es correcta, extraemos los datos del usuario de la base de datos
        tableUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("El correo electrónico no se encuentra registrado"));

        // 3. Verificamos si la cuenta está activa
        if (!user.isActive()) {
            throw new DisabledException("La cuenta se encuentra inactiva. Por favor, confirma tu email, o contactate con nosotros.");
        }

        // 4. Si todo es correcto, generamos el token de acceso exitoso
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);

        return response;

    } catch (BadCredentialsException e) {
        boolean existeEmail = userRepository.findByEmail(request.getEmail()).isPresent();
        if (!existeEmail) {
            throw new UsernameNotFoundException("El correo electrónico no se encuentra registrado");
        } else {
            throw new BadCredentialsException("La contraseña ingresada es incorrecta");
        }
    }
}

}
