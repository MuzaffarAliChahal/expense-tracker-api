package com.muzaffar.expensetracker.security;

/** The authenticated principal, built from the JWT without a database lookup. */
public record AuthUser(Long id, String email) {
}
