package com.wallet.alkemy.config;
import java.io.IOException;

import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.alkemy.dto.ErrorResponseDto;
import com.wallet.alkemy.exception.JwtValidationException;
import com.wallet.alkemy.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Component
@RequiredArgsConstructor
@Slf4j


public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
        private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());



    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,@NotNull HttpServletResponse response,@NotNull FilterChain filterChain) throws ServletException, IOException {


        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        String username;

        try {
            username = jwtService.getUsername(token);
        } catch (JwtValidationException e) {
            // Convert malformed or invalid tokens into a controlled 401 response.
            log.warn("Invalid JWT received: {}", e.getMessage());
            writeUnauthorizedResponse(response, request, "Invalid or expired token");
            return;
        }

        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            log.error("Invalid token or user already authenticated");
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails;
        try {
            // Load the current user, role, and password from the database.
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.warn("The token user no longer exists: {}", username);
            writeUnauthorizedResponse(response, request, "Invalid or expired token");
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new  UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );



        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    /** Writes a sanitized unauthorized response for invalid authentication data. */
    private void writeUnauthorizedResponse(HttpServletResponse response, HttpServletRequest request, String message) throws IOException {
        ErrorResponseDto errorBody = ErrorResponseDto.of(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                message,
                request.getRequestURI()
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}