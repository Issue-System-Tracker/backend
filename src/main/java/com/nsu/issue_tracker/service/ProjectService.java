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
        User user = userService.findById(userId);
        
        Project project = Project.builder()
                .name(request.name())
                .admin(user)
                .members(new HashSet<>())
                .build();

        // Обновляем обе стороны ManyToMany связи
        project.getMembers().add(user);
        user.getProjects().add(project);

        save(project);
    }

    @Transactional
    public void addMemberToProject(String email, Long projectId, UUID adminId) {
        Project project = findById(projectId);

        if (!project.getAdmin().getId().equals(adminId))
            throw new AccessDeniedException
                    ("Only project's admin can add members");

        User member = userService.findByEmail(email);

        // Обновляем обе стороны ManyToMany связи
        project.getMembers().add(member);
        member.getProjects().add(project);

        save(project);
    }

    @Transactional
    public void removeMemberFromProject(String email, Long projectId, UUID adminId) {
        Project project = findById(projectId);

        if (!project.getAdmin().getId().equals(adminId))
            throw new AccessDeniedException
                    ("Only project's admin can remove members");

        User member = userService.findByEmail(email);

        // Проверяем, что удаляемый пользователь не является админом проекта
        if (project.getAdmin().getId().equals(member.getId()))
            throw new AccessDeniedException
                    ("Cannot remove project admin from project");

        // Проверяем, что пользователь является участником проекта
        if (!project.getMembers().contains(member))
            throw new EntityNotFoundException
                    ("User is not a member of this project");

        // Обновляем обе стороны ManyToMany связи
        project.getMembers().remove(member);
        member.getProjects().remove(project);

        save(project);
    }

    public Set<UserProjectsResponse> getProjectsByUser(UUID userId) {
        return userService.findById(userId).getProjects()
                .stream().map(p ->
                        UserProjectsResponse.builder()
                                .id(p.getId())
                                .adminEmail(p.getAdmin().getEmail())
                                .members(p.getMembers().stream().map(User::getEmail).toList())
                                .name(p.getName())
                                .isAdmin(p.getAdmin().getId().equals(userId))
                                .build()
                ).collect(Collectors.toSet());
    }


    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    public void delete(Long projectId, UUID userId) {
        Project project = findById(projectId);


        if (!project.getAdmin().getId().equals(userId))
            throw new AccessDeniedException("Only admin of project can delete project");

        projectRepository.delete(getReference(projectId));
    }

    public void save(Project project) {
        projectRepository.save(project);
    }

    public Project getReference(Long id) {
       return projectRepository.getReferenceById(id);
    }
}
