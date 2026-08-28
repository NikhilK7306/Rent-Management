package com.rentms.security;

import com.rentms.entity.User;
import com.rentms.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        
        // Debug logging
        System.out.println("JWT Filter - Request URI: " + request.getRequestURI());
        System.out.println("JWT Filter - Auth Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("JWT Filter - No valid Bearer token, continuing without authentication");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("JWT Filter - Token extracted: " + token.substring(0, Math.min(20, token.length())) + "...");

        try {
            String mobileNumber = jwtService.extractMobileNumber(token);

            if (mobileNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByMobileNumber(mobileNumber).orElse(null);

                if (user != null && jwtService.isTokenValid(token, user)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JWT Filter - Authentication set for user: " + mobileNumber);
                } else {
                    System.out.println("JWT Filter - Token invalid or user not found");
                }
            }
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT Filter - Token validation error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}