package com.nsu.issue_tracker.authorization.dto;

import com.nsu.issue_tracker.model.UserRole;

import java.util.Set;

public record LoginResponse(
        JwtResponse jwtResponse,
        Set<UserRole> role)
{};
