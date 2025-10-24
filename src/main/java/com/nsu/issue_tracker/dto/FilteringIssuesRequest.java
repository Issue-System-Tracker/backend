package com.nsu.issue_tracker.dto;

import com.nsu.issue_tracker.model.IssueStatus;
import jakarta.validation.constraints.Email;

public record FilteringIssuesRequest(
        IssueStatus status,

        @Email
        String assigneeEmail,

        Long sprintId
) {
}
