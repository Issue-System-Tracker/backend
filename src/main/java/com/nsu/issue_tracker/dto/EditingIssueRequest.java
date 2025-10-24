package com.nsu.issue_tracker.dto;

import com.nsu.issue_tracker.model.IssueStatus;
import com.nsu.issue_tracker.model.IssueType;
import com.nsu.issue_tracker.model.Priority;
import com.nsu.issue_tracker.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EditingIssueRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private IssueType type;

    private IssueStatus status;

    private Priority priority;


    private String assigneeEmail;

    private LocalDate startDate;
    private LocalDate endDate;

    private Long sprintId;
}
