package com.nsu.issue_tracker.service;

import com.nsu.issue_tracker.dto.SprintData;
import com.nsu.issue_tracker.model.Project;
import com.nsu.issue_tracker.model.Sprint;
import com.nsu.issue_tracker.model.User;
import com.nsu.issue_tracker.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SprintService {
    private final SprintRepository sprintRepository;
    private final ProjectService projectService;
    private final UserService userService;

    public void createSprint(Long projectId, UUID userId, SprintData request) {
        Project project = projectService.findById(projectId);

        if (!project.getAdmin().getId().equals(userId))
            throw new AccessDeniedException
                    ("You can create sprint only if you are member of the project");
        save(
                Sprint.builder()
                        .name(request.name())
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .project(projectService.getReference(projectId))
                        .build()
        );
    }

    public void editSprint(
            Long projectId, UUID userId, SprintData request, Long sprintId) {
        Project project = projectService.findById(projectId);

        if (!project.getAdmin().getId().equals(userId))
            throw new AccessDeniedException
                    ("You can edit sprint only if you are member of the project");

        save(Sprint.builder()
                .id(sprintId)
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build()
        );
    }

    public List<SprintData> findAll(Long projectId) {
        return sprintRepository
                .findAllByEndDateAfterAndProject(
                        LocalDate.now(ZoneOffset.UTC),
                        projectService.getReference(projectId))
                .stream().map(s -> SprintData.builder()
                        .sprintId(s.getId())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .name(s.getName())
                        .build())
                .toList();
    }

    public void save(Sprint sprint) {
        sprintRepository.save(sprint);
    }

    public Sprint getReference(Long id) {
        return id != null
                ? sprintRepository.getReferenceById(id)
                : null;
    }
}
