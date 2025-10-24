package com.nsu.issue_tracker.repository;

import com.nsu.issue_tracker.model.Project;
import com.nsu.issue_tracker.model.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findAllByEndDateAfterAndProject(LocalDate date, Project project);

}
