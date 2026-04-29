package com.nsu.issue_tracker.service;

import com.nsu.issue_tracker.dto.CreatingProjectRequest;
import com.nsu.issue_tracker.model.Project;
import com.nsu.issue_tracker.model.User;
import com.nsu.issue_tracker.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProjectShouldAddCreatorToMembersAndSaveProject() {
        UUID userId = UUID.randomUUID();
        User admin = createUser(userId, "admin@issue.local");
        when(userService.findById(userId)).thenReturn(admin);

        projectService.createProject(new CreatingProjectRequest("Backend Team"), userId);

        verify(projectRepository).save(any(Project.class));
        assertEquals(1, admin.getProjects().size());
        Project createdProject = admin.getProjects().iterator().next();
        assertEquals("Backend Team", createdProject.getName());
        assertTrue(createdProject.getMembers().contains(admin));
        assertEquals(admin, createdProject.getAdmin());
    }

    @Test
    void addMemberToProjectShouldThrowWhenCurrentUserIsNotAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        Project project = createProject(11L, adminId);
        when(projectRepository.findById(11L)).thenReturn(Optional.of(project));

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.addMemberToProject("member@issue.local", 11L, anotherUserId)
        );

        verify(userService, never()).findByEmail("member@issue.local");
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void removeMemberFromProjectShouldThrowWhenMemberIsNotInProject() {
        UUID adminId = UUID.randomUUID();
        User admin = createUser(adminId, "admin@issue.local");
        Project project = createProject(7L, adminId);
        project.setAdmin(admin);
        User outsider = createUser(UUID.randomUUID(), "outsider@issue.local");

        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));
        when(userService.findByEmail("outsider@issue.local")).thenReturn(outsider);

        assertThrows(
                EntityNotFoundException.class,
                () -> projectService.removeMemberFromProject("outsider@issue.local", 7L, adminId)
        );

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void removeMemberFromProjectShouldThrowWhenTryingToRemoveAdmin() {
        UUID adminId = UUID.randomUUID();
        User admin = createUser(adminId, "admin@issue.local");
        Project project = createProject(5L, adminId);
        project.setAdmin(admin);
        project.setMembers(new HashSet<>(Set.of(admin)));

        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));
        when(userService.findByEmail("admin@issue.local")).thenReturn(admin);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.removeMemberFromProject("admin@issue.local", 5L, adminId)
        );

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void removeMemberFromProjectShouldRemoveMemberAndSave() {
        UUID adminId = UUID.randomUUID();
        User admin = createUser(adminId, "admin@issue.local");
        User member = createUser(UUID.randomUUID(), "member@issue.local");
        Project project = createProject(3L, adminId);
        project.setAdmin(admin);
        project.setMembers(new HashSet<>(Set.of(admin, member)));
        member.setProjects(new HashSet<>(Set.of(project)));

        when(projectRepository.findById(3L)).thenReturn(Optional.of(project));
        when(userService.findByEmail("member@issue.local")).thenReturn(member);

        projectService.removeMemberFromProject("member@issue.local", 3L, adminId);

        assertTrue(!project.getMembers().contains(member));
        assertTrue(!member.getProjects().contains(project));
        verify(projectRepository).save(project);
    }

    private static User createUser(UUID id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setProjects(new HashSet<>());
        return user;
    }

    private static Project createProject(Long id, UUID adminId) {
        Project project = new Project();
        project.setId(id);
        project.setName("Project");
        project.setMembers(new HashSet<>());
        User admin = createUser(adminId, "admin@issue.local");
        project.setAdmin(admin);
        return project;
    }
}
