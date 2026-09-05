package com.wallet.alkemy.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wallet.alkemy.config.tableUserRole;
import com.wallet.alkemy.dto.LoginRequest;
import com.wallet.alkemy.dto.LoginResponseDTO;
import com.wallet.alkemy.dto.UserDTO;
import com.wallet.alkemy.models.tableUser;
import com.wallet.alkemy.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BankAccountService bankAccountService;
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

        response.put("status", "success");
        response.put("data", Map.of("email", user.getEmail()));

        return ResponseEntity.ok(response);
    }

    /** Authenticates a user and returns a signed JWT. */
    public LoginResponseDTO login(LoginRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email o contraseña inválidos"));

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);

        return response;
    }
}
