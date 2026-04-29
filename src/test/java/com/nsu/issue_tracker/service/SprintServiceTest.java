package com.nsu.issue_tracker.service;

import com.nsu.issue_tracker.dto.SprintData;
import com.nsu.issue_tracker.model.Project;
import com.nsu.issue_tracker.model.Sprint;
import com.nsu.issue_tracker.model.User;
import com.nsu.issue_tracker.repository.SprintRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SprintService sprintService;

    @Test
    void createSprintShouldThrowForNonAdminUser() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Project project = createProject(10L, adminId);
        when(projectService.findById(10L)).thenReturn(project);

        assertThrows(
                AccessDeniedException.class,
                () -> sprintService.createSprint(10L, memberId, sampleRequest("Sprint 1"))
        );
    }

    @Test
    void createSprintShouldSaveSprintForAdmin() {
        UUID adminId = UUID.randomUUID();
        Project project = createProject(10L, adminId);
        when(projectService.findById(10L)).thenReturn(project);
        when(projectService.getReference(10L)).thenReturn(project);

        sprintService.createSprint(10L, adminId, sampleRequest("Sprint Alpha"));

        ArgumentCaptor<Sprint> captor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(captor.capture());
        assertEquals("Sprint Alpha", captor.getValue().getName());
        assertEquals(project, captor.getValue().getProject());
    }

    @Test
    void editSprintShouldSaveUpdatedEntityForAdmin() {
        UUID adminId = UUID.randomUUID();
        Project project = createProject(11L, adminId);
        when(projectService.findById(11L)).thenReturn(project);

        sprintService.editSprint(11L, adminId, sampleRequest("Edited Sprint"), 44L);

        ArgumentCaptor<Sprint> captor = ArgumentCaptor.forClass(Sprint.class);
        verify(sprintRepository).save(captor.capture());
        assertEquals(44L, captor.getValue().getId());
        assertEquals("Edited Sprint", captor.getValue().getName());
    }

    @Test
    void findAllShouldMapRepositoryEntitiesToDto() {
        Project project = createProject(20L, UUID.randomUUID());
        Sprint sprint = Sprint.builder()
                .id(100L)
                .name("Current")
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 15))
                .project(project)
                .build();
        when(projectService.getReference(20L)).thenReturn(project);
        when(sprintRepository.findAllByEndDateAfterAndProject(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(project)))
                .thenReturn(List.of(sprint));

        List<SprintData> result = sprintService.findAll(20L);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).sprintId());
        assertEquals("Current", result.get(0).name());
    }

    @Test
    void getReferenceShouldReturnNullForNullId() {
        assertEquals(null, sprintService.getReference(null));
    }

    @Test
    void editSprintShouldThrowForNonAdmin() {
        UUID adminId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Project project = createProject(25L, adminId);
        when(projectService.findById(25L)).thenReturn(project);

        assertThrows(
                AccessDeniedException.class,
                () -> sprintService.editSprint(25L, memberId, sampleRequest("Locked"), 1L)
        );
    }

    @Test
    void getReferenceShouldDelegateForNonNullId() {
        Sprint sprint = Sprint.builder().id(77L).build();
        when(sprintRepository.getReferenceById(77L)).thenReturn(sprint);

        Sprint result = sprintService.getReference(77L);
        assertEquals(77L, result.getId());
    }

    private static SprintData sampleRequest(String name) {
        return SprintData.builder()
                .name(name)
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 14))
                .build();
    }

    private static Project createProject(Long id, UUID adminId) {
        User admin = new User();
        admin.setId(adminId);
        admin.setEmail("admin@issue.local");

        Project project = new Project();
        project.setId(id);
        project.setAdmin(admin);
        return project;
    }
}
