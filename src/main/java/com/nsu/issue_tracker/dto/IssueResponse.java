package com.nsu.issue_tracker.dto;

import com.nsu.issue_tracker.model.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class IssueResponse {
    private Long id;

    private String title;

    private String description;

    private IssueType type;

    private IssueStatus status;

    private Priority priority;

    private String authorEmail;

    private String assigneeEmail;

    private LocalDate startDate;
    private LocalDate endDate;

    private Long sprintId;

    private Long projectId;

}
