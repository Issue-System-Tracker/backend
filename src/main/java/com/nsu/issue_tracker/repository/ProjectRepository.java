package com.nsu.issue_tracker.repository;

import com.nsu.issue_tracker.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
