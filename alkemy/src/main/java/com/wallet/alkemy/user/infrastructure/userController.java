package com.wallet.alkemy.user.infrastructure;

import com.wallet.alkemy.service.JwtService;
import com.wallet.alkemy.user.infrastructure.dto.LoginRequestDto;
import com.wallet.alkemy.user.infrastructure.dto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j


public class userController {

    private final JwtService jwtService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> getUserById(@RequestBody LoginRequestDto loginRequestDto) {

        UserDetails user = User.withDefaultPasswordEncoder()
                .username(loginRequestDto.getEmail())
                .password(loginRequestDto.getPassword())
                .roles("USER")
                .build();


        String token = jwtService.generateToken(user);

        LoginResponseDto loginResponseDto = new LoginResponseDto();
        loginResponseDto.setToken(token);



        return ResponseEntity.ok(loginResponseDto);
    }
}
