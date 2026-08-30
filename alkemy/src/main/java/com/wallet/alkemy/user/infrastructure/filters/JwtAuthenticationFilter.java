package com.wallet.alkemy.user.infrastructure.filters;
import com.wallet.alkemy.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j


public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;



    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,@NotNull HttpServletResponse response,@NotNull FilterChain filterChain) throws ServletException, IOException {


        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {

            log.info("token not found");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        String username = jwtService.getUsername(token);

        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            log.error("Invalid token or user already authenticated");
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username(username)
                .password("password")
                .roles("USER")
                .build();

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
}
