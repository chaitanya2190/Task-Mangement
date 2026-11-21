package com.taskapp.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String STRONG_SECRET = "dGhpcy1pcy1hLXN1cGVyLXNlY3JldC1rZXktdGhhdC1zaG91bGQtYmUtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZw==";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", STRONG_SECRET);
        jwtService.validateSecret();
    }

    @Test
    void validateSecret_FailsWhenEmpty() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", "");
        assertThrows(IllegalStateException.class, service::validateSecret);
    }

    @Test
    void validateSecret_FailsWhenFallbackDetected() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", "fallback-dev-secret-change-me-to-something-secure-minimum-256-bits");
        assertThrows(IllegalStateException.class, service::validateSecret);
    }

    @Test
    void validateSecret_FailsWhenTooShort() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", "short-secret");
        assertThrows(IllegalStateException.class, service::validateSecret);
    }

    @Test
    void generateAndValidateToken_Success() {
        UserDetails userDetails = new User("admin@test.com", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("admin@test.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }
}
