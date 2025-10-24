package com.nsu.issue_tracker.dto;

import com.nsu.issue_tracker.model.User;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
public record UserProjectsResponse(
        Long id,
        String name,
        List<String> members,
        String adminEmail,
        Boolean isAdmin
) {
}
