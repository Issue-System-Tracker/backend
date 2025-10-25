package com.nsu.issue_tracker.controller;

import com.nsu.issue_tracker.authorization.security.CustomUserDetails;
import com.nsu.issue_tracker.dto.SprintData;
import com.nsu.issue_tracker.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
@RequiredArgsConstructor
public class SprintController {
    private final SprintService sprintService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createSprint(
            @PathVariable Long projectId,
            @RequestBody @Valid SprintData request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        sprintService.createSprint(
                projectId,
                UUID.fromString(userDetails.getUserId()),
                request);
    }

    @PutMapping("/{id}")
    public void editSprint(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @RequestBody @Valid SprintData request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        sprintService.editSprint(
                projectId,
                UUID.fromString(userDetails.getUserId()),
                request,
                id);
    }

    @GetMapping
    public ResponseEntity<?> getAllSprints(
            @PathVariable Long projectId) {
        return ResponseEntity.ok()
                .body(sprintService.findAll(projectId));
    }
}
