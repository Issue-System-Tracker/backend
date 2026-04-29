package com.nsu.issue_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsu.issue_tracker.authorization.security.CustomUserDetails;
import com.nsu.issue_tracker.dto.SprintData;
import com.nsu.issue_tracker.service.SprintService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SprintController.class)
@AutoConfigureMockMvc(addFilters = false)
class SprintControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SprintService sprintService;

    @Test
    void createSprintShouldReturnCreatedStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        SprintData payload = SprintData.builder()
                .name("Sprint A")
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 14))
                .build();

        mockMvc.perform(post("/api/projects/10/sprints")
                        .with(authentication(auth(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        verify(sprintService).createSprint(10L, userId, payload);
    }

    @Test
    void createSprintShouldReturnBadRequestWhenDateMissing() throws Exception {
        UUID userId = UUID.randomUUID();
        String payload = """
                {
                  "name": "Sprint A",
                  "startDate": "2026-04-01"
                }
                """;

        mockMvc.perform(post("/api/projects/10/sprints")
                        .with(authentication(auth(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editSprintShouldReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        SprintData payload = SprintData.builder()
                .name("Sprint Updated")
                .startDate(LocalDate.of(2026, 5, 1))
                .endDate(LocalDate.of(2026, 5, 15))
                .build();

        mockMvc.perform(put("/api/projects/10/sprints/8")
                        .with(authentication(auth(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        verify(sprintService).editSprint(10L, userId, payload, 8L);
    }

    @Test
    void getAllSprintsShouldReturnJsonArray() throws Exception {
        when(sprintService.findAll(5L)).thenReturn(List.of(
                SprintData.builder()
                        .name("Sprint List")
                        .sprintId(1L)
                        .startDate(LocalDate.of(2026, 4, 1))
                        .endDate(LocalDate.of(2026, 4, 14))
                        .build()
        ));

        mockMvc.perform(get("/api/projects/5/sprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sprint List"))
                .andExpect(jsonPath("$[0].sprintId").value(1L));
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
