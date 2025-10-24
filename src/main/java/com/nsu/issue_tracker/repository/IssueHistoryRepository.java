package com.nsu.issue_tracker.repository;

import com.nsu.issue_tracker.model.Issue;
import com.nsu.issue_tracker.model.IssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueHistoryRepository extends JpaRepository<IssueHistory, Long> {
    List<IssueHistory> findAllByIssue(Issue issue);
}
