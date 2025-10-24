package com.nsu.issue_tracker.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record IssueChangesResponse(
        String fieldName,
        String oldValue,
        String newValue,
        String changedBy,
        LocalDateTime changedAt
) { }
