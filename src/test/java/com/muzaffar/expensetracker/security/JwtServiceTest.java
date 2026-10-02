package com.muzaffar.expensetracker.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtMTIz";

    private final JwtService jwt = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(5)));

    @Test
    void roundTripsUserIdAndEmail() {
        String token = jwt.generate(42L, "ali@example.com");
        assertThat(jwt.parse(token)).contains(new AuthUser(42L, "ali@example.com"));
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwt.generate(42L, "ali@example.com");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");
        assertThat(jwt.parse(tampered)).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        var expired = new JwtService(new JwtProperties(SECRET, Duration.ofSeconds(-1)));
        assertThat(expired.parse(expired.generate(1L, "a@b.com"))).isEmpty();
    }

    @Test
    void rejectsGarbage() {
        assertThat(jwt.parse("not-a-jwt")).isEmpty();
        assertThat(jwt.parse("")).isEmpty();
    }
}
