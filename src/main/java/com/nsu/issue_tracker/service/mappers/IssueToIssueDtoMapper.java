package com.nsu.issue_tracker.service.mappers;

import com.nsu.issue_tracker.dto.IssueResponse;
import com.nsu.issue_tracker.model.Issue;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class IssueToIssueDtoMapper implements Function<Issue, IssueResponse> {
    @Override
    public IssueResponse apply(Issue issue) {
        return IssueResponse.builder()
                .id(issue.getId())
                .assigneeEmail(issue.getAssignee() != null
                        ? issue.getAssignee().getEmail()
                        :null)
                .authorEmail(issue.getAuthor().getEmail())
                .title(issue.getTitle())
                .type(issue.getType())
                .description(issue.getDescription())
                .status(issue.getStatus())
                .startDate(issue.getStartDate())
                .endDate(issue.getEndDate())
                .projectId(issue.getProject().getId())
                .sprintId(issue.getSprint() != null
                        ? issue.getSprint().getId()
                        : null)
                .priority(issue.getPriority())
                .build();
    }
}
