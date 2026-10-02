package com.muzaffar.expensetracker.auth;

import com.muzaffar.expensetracker.auth.AuthDtos.AuthResponse;
import com.muzaffar.expensetracker.auth.AuthDtos.LoginRequest;
import com.muzaffar.expensetracker.auth.AuthDtos.RegisterRequest;
import com.muzaffar.expensetracker.common.ConflictException;
import com.muzaffar.expensetracker.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered");
        }
        User user = users.save(new User(email, passwordEncoder.encode(request.password()), request.fullName().trim()));
        return token(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return token(user);
    }

    private AuthResponse token(User user) {
        return new AuthResponse(jwtService.generate(user.getId(), user.getEmail()), "Bearer",
                jwtService.expiration().toSeconds());
    }
}
