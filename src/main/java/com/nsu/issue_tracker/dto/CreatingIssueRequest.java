package com.nsu.issue_tracker.dto;

import com.nsu.issue_tracker.model.IssueType;
import com.nsu.issue_tracker.model.Priority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreatingIssueRequest(

        @NotBlank
        @Size(min = 3, max = 100)
        String title,

        @NotBlank
        @Size(min = 5, max = 1000)
        String description,

        @NotNull
        IssueType type,

        String assigneeEmail,

        Priority priority,

        LocalDate startDate,
        LocalDate endDate,

        Long sprintId

        // TODO ..
) {}
