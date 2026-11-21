package com.industrial.maintenance.config;

import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtClaims {

    private JwtClaims() {
        // utility class: not meant to be instantiated
    }

    /** Returns the employeeId claim from the logged-in user's token, or null if absent. */
    public static Long employeeId(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        Number employeeId = jwt.getClaim("employeeId");
        return employeeId != null ? employeeId.longValue() : null;
    }
}