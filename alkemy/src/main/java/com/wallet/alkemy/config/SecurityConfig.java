package com.wallet.alkemy.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.wallet.alkemy.service.DatabaseUserDetailsService;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DatabaseUserDetailsService databaseUserDetailsService;

    /** Configures stateless JWT-based HTTP security. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Permite absolutamente todas las peticiones OPTIONS (Preflight) a cualquier ruta
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                        
                        .requestMatchers(
                                "/api/public/**", 
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/check-session",
                                "/**/check-session",
                                "/api/auth/activate"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").permitAll() //dashboard libre temporalmente para pruebas locales
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** Provides the password encoder used for stored credentials. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Creates the DAO authentication provider backed by the database user service. */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(databaseUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** Exposes Spring Security's configured authentication manager. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /** Configures allowed browser origins, methods, and headers. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Explicitly allow the common Live Server origins.
        configuration.setAllowedOrigins(List.of(
            "http://127.0.0.1:5500", 
            "http://localhost:5500",
            "http://127.0.0.1:5501",
            "http://localhost:5501"
        )); 
        
        //requerido por la API para los cambios de estado lógico
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Allow standard headers required for JSON and token requests.
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
        
        // Allow cookies or credentials if they are needed later.
        configuration.setAllowCredentials(true);

        // Permite que el cliente reciba los tokens de forma expuesta en las respuestas HTTP
        configuration.setExposedHeaders(List.of("Authorization", "Refresh-Token")); 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all endpoints.
        return source;
    }
}
