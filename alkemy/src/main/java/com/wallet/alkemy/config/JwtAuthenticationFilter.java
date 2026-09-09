package com.wallet.alkemy.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wallet.alkemy.dto.ErrorResponseDto;
import com.wallet.alkemy.exception.JwtValidationException;
import com.wallet.alkemy.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

@Override
protected void doFilterInternal(
        @NonNull HttpServletRequest request, 
        @NonNull HttpServletResponse response, 
        @NonNull FilterChain filterChain) throws ServletException, IOException {

    final String authorization = request.getHeader("Authorization");

    // 1. Tolerancia a ausencias de token y strings null (Dejar pasar si no hay intención de autenticarse)
    // Dejamos pasar con doFilter para que endpoints públicos como /login funcionen.
    if (authorization == null 
            || !authorization.startsWith("Bearer ") 
            || authorization.equalsIgnoreCase("Bearer null") 
            || authorization.trim().length() <= 7) {
        
        filterChain.doFilter(request, response);
        return; // Detiene la ejecución de este filtro de manera limpia
    }

    final String jwt = authorization.substring(7);

    try {
        // 2. Intentamos extraer el usuario del Token utilizando JwtService
        final String username = jwtService.getUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Si todo sale bien, continuamos con la cadena de filtros 
        filterChain.doFilter(request, response);

    } catch (JwtValidationException e) {
        // método privado para pintar el JSON estructurado con estado 401
        writeUnauthorizedResponse(response, request, e.getMessage());
        
        // NO llamamos a filterChain.doFilter(). Cortamos el flujo aquí de forma segura.
        return; 
    }
}

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
