package com.nsu.issue_tracker.repository;

import com.nsu.issue_tracker.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    @Query("""
    SELECT i FROM Issue i
    WHERE i.project.id = :projectId
      AND (:status IS NULL OR i.status = :status)
      AND (:assignee IS NULL OR i.assignee = :assignee)
      AND (:sprintId IS NULL OR i.sprint.id = :sprintId)
""")
    List<Issue> findFilteredIssues(@Param("projectId") Long projectId,
                                   @Param("status") IssueStatus status,
                                   @Param("assignee") User assignee,
                                   @Param("sprintId") Long sprintId);


    List<Issue> findAllBySprintAndProject(Sprint sprint, Project project);

    List<Issue> findAllByProject(Project project);

}
