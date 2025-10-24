package com.nsu.issue_tracker.service;

import com.nsu.issue_tracker.dto.CreatingProjectRequest;
import com.nsu.issue_tracker.dto.UserProjectsResponse;
import com.nsu.issue_tracker.model.Project;
import com.nsu.issue_tracker.model.User;
import com.nsu.issue_tracker.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;

    @Transactional
    public void createProject(CreatingProjectRequest request, UUID userId) {
        Project project = Project.builder()
                .name(request.name())
                .admin(userService.getReferenceById(userId))
                .members(new HashSet<>())
                .build();

        User user = userService.findById(userId);

        user.getProjects().add(project);

        project.getMembers().add(userService.getReferenceById(userId));

        save(project);
    }

    public void addMemberToProject(String email, Long projectId, UUID adminId) {
        Project project = findById(projectId);

        if (!project.getAdmin().getId().equals(adminId))
            throw new AccessDeniedException
                    ("Only project's admin can add members");

        User member = userService.findByEmail(email);

        project.getMembers().add(member);

        save(project);
    }

    public Set<UserProjectsResponse> getProjectsByUser(UUID userId) {
        return userService.findById(userId).getProjects()
                .stream().map(p ->
                        UserProjectsResponse.builder()
                                .id(p.getId())
                                .adminEmail(p.getAdmin().getEmail())
                                .members(p.getMembers())
                                .name(p.getName())
                                .isAdmin(p.getAdmin().getId().equals(userId))
                                .build()
                ).collect(Collectors.toSet());
    }


    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    public void save(Project project) {
        projectRepository.save(project);
    }

    public Project getReference(Long id) {
       return projectRepository.getReferenceById(id);
    }
}
