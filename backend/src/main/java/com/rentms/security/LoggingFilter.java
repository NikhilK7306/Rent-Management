package com.rentms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Request: {} {}", request.getMethod(), request.getRequestURI());
        log.info("Content-Type: {}", request.getContentType());
        log.info("Content-Length: {}", request.getContentLength());

        filterChain.doFilter(request, response);
        log.info("Response status: {}", response.getStatus());
    }
}