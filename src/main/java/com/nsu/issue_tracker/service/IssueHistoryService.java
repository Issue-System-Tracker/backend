package com.nsu.issue_tracker.service;

import com.nsu.issue_tracker.dto.EditingIssueRequest;
import com.nsu.issue_tracker.dto.IssueChangesResponse;
import com.nsu.issue_tracker.model.Issue;
import com.nsu.issue_tracker.model.IssueHistory;
import com.nsu.issue_tracker.repository.IssueHistoryRepository;
import com.nsu.issue_tracker.repository.IssueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IssueHistoryService {
    private final IssueHistoryRepository issueHistoryRepository;
    private final IssueRepository issueRepository;

    @Transactional
    public void recordChanges(EditingIssueRequest request, Issue issue, String changedBy) {
        compareAndRecord(issue, changedBy, "Title", issue.getTitle(), request.getTitle());
        compareAndRecord(issue, changedBy, "Description", issue.getDescription(), request.getDescription());
        compareAndRecord(issue, changedBy, "Type", issue.getType(), request.getType());
        compareAndRecord(issue, changedBy, "Status", issue.getStatus(), request.getStatus());
        compareAndRecord(issue, changedBy, "Priority", issue.getPriority(), request.getPriority());
        compareAndRecord(issue, changedBy, "Assignee",
                issue.getAssignee() != null ? issue.getAssignee().getEmail() : null,
                request.getAssigneeEmail());
        compareAndRecord(issue, changedBy, "StartDate", issue.getStartDate(), request.getStartDate());
        compareAndRecord(issue, changedBy, "EndDate", issue.getEndDate(), request.getEndDate());
        compareAndRecord(issue, changedBy, "SprintId",
                issue.getSprint() != null ? issue.getSprint().getId() : null,
                request.getSprintId());
    }

    @Transactional
    public void compareAndRecord(Issue issue, String userEmail, String fieldName,
                                  Object oldVal, Object newVal) {
        if (!Objects.equals(oldVal, newVal)) {
            save(IssueHistory.builder()
                    .issue(issue)
                    .changedBy(userEmail)
                    .fieldName(fieldName)
                    .oldValue(oldVal != null ? oldVal.toString() : null)
                    .newValue(newVal != null ? newVal.toString() : null)
                    .changedAt(LocalDateTime.now(ZoneOffset.UTC))
                    .build());
        }
    }

    public List<IssueChangesResponse> getChangesByIssue(Long issueId) {
        return issueHistoryRepository
                .findAllByIssue(issueRepository.getReferenceById(issueId))
                .stream()
                .map(history -> IssueChangesResponse.builder()
                        .fieldName(history.getFieldName())
                        .oldValue(history.getOldValue())
                        .newValue(history.getNewValue())
                        .changedBy(history.getChangedBy())
                        .changedAt(history.getChangedAt())
                        .build())
                .toList();
    }


    public void save(IssueHistory issueHistory) {
        issueHistoryRepository.save(issueHistory);
    }

    @Transactional
    public void deleteByIssue(Issue issue) {
        issueHistoryRepository.deleteByIssue(issue);
    }
}
