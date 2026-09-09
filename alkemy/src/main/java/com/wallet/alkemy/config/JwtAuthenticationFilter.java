package com.wallet.alkemy.config;

import java.io.IOException;

import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        // MODIFICACIÓN 1: Tolerancia a ausencias de token y strings "null" provenientes del front
        if (authorization == null || !authorization.startsWith("Bearer ") || 
            authorization.equalsIgnoreCase("Bearer null") || authorization.trim().length() <= 7) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        String username;

        try {
            username = jwtService.getUsername(token);
        } catch (JwtValidationException e) {
            log.warn("JWT inválido recibido: {}", e.getMessage());
            
            // MODIFICACIÓN 2: No cortamos el flujo abruptamente. Dejamos que pase la petición.
            // Si la ruta es protegida, Spring Security devolverá el 403 de forma nativa.
            // Si la ruta es pública (/check-session), llegará al controller para responder según tu lógica.
            filterChain.doFilter(request, response);
            return;
        }

        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.warn("El usuario del token ya no existe: {}", username);
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // =========================================================================
        // RENOVACIÓN DE TOKEN: Si está autenticado, envía un token con 10 minutos nuevos
        // =========================================================================
        try {
            String tokenFresco = jwtService.generateToken(userDetails);
            response.setHeader("Refresh-Token", tokenFresco);
        } catch (Exception e) {
            log.error("Error al generar el Sliding Token: {}", e.getMessage());
        }
        // =========================================================================

        filterChain.doFilter(request, response);
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
