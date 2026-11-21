package com.industrial.maintenance.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] STAFF = {"ADMIN", "MANAGER", "TECHNICIAN", "OPERATOR"};

    private final SecurityErrorHandler securityErrorHandler;

    public SecurityConfig(SecurityErrorHandler securityErrorHandler) {
        this.securityErrorHandler = securityErrorHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes long");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // Reads the "role" claim from the token and turns it into a Spring role, e.g. ROLE_TECHNICIAN
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/api/auth/login", "/api/health").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Sensor readings: devices may submit, and nothing else
                        .requestMatchers(HttpMethod.POST, "/api/equipment/*/readings").hasAnyRole("SENSOR", "ADMIN")

                        // Dashboard
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**").hasAnyRole("MANAGER", "ADMIN")

                        // Employees and factories
                        .requestMatchers(HttpMethod.GET, "/api/employees/**", "/api/factories/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/employees/**", "/api/factories/**").hasRole("ADMIN")

                        // Equipment and spare parts (includes readings, alerts, history and cost GETs)
                        .requestMatchers(HttpMethod.GET, "/api/equipment/**", "/api/spare-parts/**").hasAnyRole(STAFF)
                        .requestMatchers("/api/equipment/**", "/api/spare-parts/**").hasRole("ADMIN")

                        // Service requests
                        .requestMatchers(HttpMethod.GET, "/api/service-requests/**").hasAnyRole(STAFF)
                        .requestMatchers(HttpMethod.POST, "/api/service-requests").hasAnyRole("OPERATOR", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/service-requests/*/assign").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/service-requests/*/cancel").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/service-requests/*/start", "/api/service-requests/*/hold",
                                "/api/service-requests/*/resume", "/api/service-requests/*/complete")
                        .hasAnyRole("TECHNICIAN", "MANAGER")

                        // Maintenance schedules
                        .requestMatchers(HttpMethod.GET, "/api/maintenance-schedules/**").hasAnyRole(STAFF)
                        .requestMatchers(HttpMethod.POST, "/api/maintenance-schedules/*/complete").hasAnyRole("TECHNICIAN", "MANAGER")
                        .requestMatchers("/api/maintenance-schedules/**").hasAnyRole("MANAGER", "ADMIN")

                        // Maintenance records
                        .requestMatchers(HttpMethod.GET, "/api/maintenance-records/**").hasAnyRole(STAFF)
                        .requestMatchers(HttpMethod.POST, "/api/maintenance-records").hasAnyRole("TECHNICIAN", "MANAGER")

                        // Alerts
                        .requestMatchers(HttpMethod.GET, "/api/alerts/**").hasAnyRole(STAFF)
                        .requestMatchers(HttpMethod.PATCH, "/api/alerts/*/acknowledge", "/api/alerts/*/resolve")
                        .hasAnyRole("TECHNICIAN", "MANAGER", "ADMIN")

                        // Anything not listed above: staff only
                        .anyRequest().hasAnyRole(STAFF)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(securityErrorHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler));

        return http.build();
    }
}