package com.nsu.issue_tracker.authorization.service;


import com.nsu.issue_tracker.authorization.dto.*;
import com.nsu.issue_tracker.authorization.model.RevokedToken;
import com.nsu.issue_tracker.authorization.repository.RevokedTokenRepository;
import com.nsu.issue_tracker.model.User;
import com.nsu.issue_tracker.model.UserRole;
import com.nsu.issue_tracker.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public void register(RegistrationRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already exists");
        }


        User user = new User(
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                Set.of(UserRole.ROLE_USER)
        );

        userService.save(user);
    }

    public LoginResponse authenticate(LoginRequest request) {
        User user = userService.findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return new LoginResponse(jwtTokenService.generateTokens(user), user.getRoles());
    }

    public JwtResponse refreshToken(String refreshToken) {
        jwtTokenService.validateToken(refreshToken);

        if (jwtTokenService.isTokenRevoked(refreshToken)) {
            throw new ExpiredJwtException(null, null, "Token has been revoked");
        }

        String email = jwtTokenService.extractEmail(refreshToken);

        User user = userService.findByEmail(email);

        return jwtTokenService.generateTokens(user);
    }

    @Transactional
    public void revokeToken(String token) {
        String jti = jwtTokenService.extractJti(token);
        revokedTokenRepository.save(new RevokedToken(jti));
    }
}
