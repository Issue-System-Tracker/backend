package com.nsu.issue_tracker.controller;

import com.nsu.issue_tracker.authorization.security.CustomUserDetails;
import com.nsu.issue_tracker.dto.CreatingProjectRequest;
import com.nsu.issue_tracker.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProject(
            @RequestBody @Valid CreatingProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService
                .createProject(request, UUID.fromString(userDetails.getUserId()));
    }

    @PostMapping("/{id}/member")
    public void addMember(
            @PathVariable Long id,
            @RequestParam @Email String memberEmail,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.addMemberToProject(
                memberEmail,
                id,
                UUID.fromString(userDetails.getUserId()));
    }

    @GetMapping
    public ResponseEntity<?> getProjectsByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok().body(
                        projectService
                .getProjectsByUser(UUID.fromString(userDetails.getUserId())));
    }

    @DeleteMapping("/{projectId}")
    public void deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService
                .delete(projectId, UUID.fromString(userDetails.getUserId()));
    }

}
