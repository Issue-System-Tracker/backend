package com.nsu.issue_tracker.realtime;

import com.nsu.issue_tracker.model.IssueStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BoardRealtimeEvent(
        String type,
        Long projectId,
        Long issueId,
        IssueStatus status,
        boolean hasNonStatusChanges,
        String changedBy,
        LocalDateTime changedAt
) {
}
