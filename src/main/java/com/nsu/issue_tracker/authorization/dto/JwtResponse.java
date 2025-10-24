package com.nsu.issue_tracker.authorization.dto;

public record JwtResponse (
        String accessToken,
        String refreshToken,
        long expiresIn,
        long refreshExpiresIn
) {};
