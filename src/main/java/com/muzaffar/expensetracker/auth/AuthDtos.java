package com.muzaffar.expensetracker.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @Schema(example = "ali@example.com") @NotBlank @Email String email,
            @Schema(example = "Str0ngPassw0rd!") @NotBlank @Size(min = 8, max = 72) String password,
            @Schema(example = "Ali Khan") @NotBlank @Size(max = 120) String fullName) {
    }

    public record LoginRequest(
            @Schema(example = "ali@example.com") @NotBlank @Email String email,
            @Schema(example = "Str0ngPassw0rd!") @NotBlank String password) {
    }

    public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) {
    }
}
