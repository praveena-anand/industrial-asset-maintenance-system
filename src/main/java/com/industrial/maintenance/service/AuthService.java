package com.industrial.maintenance.service;

import com.industrial.maintenance.dto.LoginRequest;
import com.industrial.maintenance.dto.LoginResponse;
import com.industrial.maintenance.entity.Employee;
import com.industrial.maintenance.entity.EmployeeStatus;
import com.industrial.maintenance.exception.InvalidCredentialsException;
import com.industrial.maintenance.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String INVALID_LOGIN = "Invalid email or password";

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;

    public AuthService(EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder,
                       @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
    }

    public LoginResponse login(LoginRequest request) {
        Employee employee = employeeRepository.findByEmail(request.email())
                .orElseThrow(() -> failedLogin());

        if (employee.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), employee.getPasswordHash())) {
            throw failedLogin();
        }
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw failedLogin();
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("industrial-maintenance-api")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(employee.getEmployeeCode())
                .claim("employeeId", employee.getId())
                .claim("role", employee.getRole().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        log.info("Employee {} logged in", employee.getEmployeeCode());
        return new LoginResponse(token, "Bearer", expiresAt,
                employee.getEmployeeCode(), employee.getFullName(), employee.getRole());
    }

    private InvalidCredentialsException failedLogin() {
        log.warn("Failed login attempt");
        return new InvalidCredentialsException(INVALID_LOGIN);
    }
}