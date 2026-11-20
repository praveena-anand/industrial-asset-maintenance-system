package com.industrial.maintenance.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    // 401: no token, or an invalid/expired token
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        writeError(request, response, HttpStatus.UNAUTHORIZED,
                "Authentication required: provide a valid Bearer token");
    }

    // 403: valid token, but the role is not allowed to do this
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        writeError(request, response, HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action");
    }

    private void writeError(HttpServletRequest request, HttpServletResponse response,
                            HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String body = """
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}"""
                .formatted(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                        message, request.getRequestURI());
        response.getWriter().write(body);
    }
}