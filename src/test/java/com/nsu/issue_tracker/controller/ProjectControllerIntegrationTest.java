package com.nsu.issue_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsu.issue_tracker.authorization.security.CustomUserDetails;
import com.nsu.issue_tracker.dto.CreatingProjectRequest;
import com.nsu.issue_tracker.dto.UserProjectsResponse;
import com.nsu.issue_tracker.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @Test
    void createProjectShouldReturnCreatedAndDelegateToService() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/projects")
                        .with(authentication(auth(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatingProjectRequest("Platform team"))))
                .andExpect(status().isCreated());

        verify(projectService).createProject(eq(new CreatingProjectRequest("Platform team")), eq(userId));
    }

    @Test
    void addMemberShouldValidateEmailAndReturnBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/projects/1/member")
                        .with(authentication(auth(userId)))
                        .queryParam("memberEmail", "broken-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMemberShouldDelegateToServiceForValidEmail() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/projects/9/member")
                        .with(authentication(auth(userId)))
                        .queryParam("memberEmail", "member@issue.local"))
                .andExpect(status().isOk());

        verify(projectService).addMemberToProject("member@issue.local", 9L, userId);
    }

    @Test
    void removeMemberShouldDelegateToService() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/projects/9/member")
                        .with(authentication(auth(userId)))
                        .queryParam("memberEmail", "member@issue.local"))
                .andExpect(status().isOk());

        verify(projectService).removeMemberFromProject("member@issue.local", 9L, userId);
    }

    @Test
    void createProjectShouldReturnBadRequestForShortName() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/projects")
                        .with(authentication(auth(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatingProjectRequest("ab"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProjectsByUserShouldReturnProjectsPayload() throws Exception {
        UUID userId = UUID.randomUUID();
        when(projectService.getProjectsByUser(userId)).thenReturn(Set.of(
                UserProjectsResponse.builder()
                        .id(1L)
                        .name("Alpha")
                        .adminEmail("admin@issue.local")
                        .members(List.of("admin@issue.local", "dev@issue.local"))
                        .isAdmin(true)
                        .build()
        ));

        mockMvc.perform(get("/api/projects")
                        .with(authentication(auth(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alpha"))
                .andExpect(jsonPath("$[0].adminEmail").value("admin@issue.local"));
    }

    @Test
    void deleteProjectShouldDelegateToService() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/projects/15")
                        .with(authentication(auth(userId))))
                .andExpect(status().isOk());

        verify(projectService).delete(15L, userId);
    }

    private static Authentication auth(UUID userId) {
        CustomUserDetails principal = new CustomUserDetails(
                userId.toString(),
                "test@issue.local",
                List.of()
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
