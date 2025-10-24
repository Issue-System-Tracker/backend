package com.nsu.issue_tracker.controller;

import com.nsu.issue_tracker.authorization.security.CustomUserDetails;
import com.nsu.issue_tracker.dto.CreatingIssueRequest;
import com.nsu.issue_tracker.dto.EditingIssueRequest;
import com.nsu.issue_tracker.dto.FilteringIssuesRequest;
import com.nsu.issue_tracker.model.IssueStatus;
import com.nsu.issue_tracker.model.UserRole;
import com.nsu.issue_tracker.service.IssueHistoryService;
import com.nsu.issue_tracker.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
@RequiredArgsConstructor
public class IssueController {
    private final IssueService issueService;
    private final IssueHistoryService issueHistoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createIssue(
            @PathVariable Long projectId,
            @RequestBody @Valid CreatingIssueRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        issueService.createIssue(
                request, projectId, UUID.fromString(userDetails.getUserId()));
    }

    @GetMapping
    public ResponseEntity<?> getAllIssues(
            @PathVariable Long projectId
            ) {
        return ResponseEntity.ok()
                .body(issueService.getAllIssues(projectId));
    }

    @GetMapping("/sprint/{id}")
    public ResponseEntity<?> getIssuesBySprint(
            @PathVariable Long projectId,
            @PathVariable Long id) {
        return ResponseEntity.ok()
                .body(issueService.getIssuesBySprint(projectId, id));
    }

    @PutMapping("/{id}")
    public void editIssue(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @RequestBody @Valid EditingIssueRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        issueService.editIssue(
                request,
                id,
                UUID.fromString(userDetails.getUserId()),
                projectId);
    }

    @PostMapping("/delete/{id}")
    public void deleteIssue(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        issueService.deleteIssue(
                projectId,
                id,
                UUID.fromString(userDetails.getUserId()));
    }

    @PostMapping("/search")
    public ResponseEntity<?> getFilteredIssues(
            @PathVariable Long projectId,
            @RequestBody @Valid FilteringIssuesRequest request) {
        return ResponseEntity.ok().body(issueService.filterIssues(projectId, request));
    }

    @PutMapping("/{id}/test")
    public void sendToTesting(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        issueService.sendIssueToTesting(projectId, id, userDetails.getUsername());
    }

    @PutMapping("/{id}/progress")
    public void sendToProgress(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        issueService.sendIssueToProgress(projectId, id, userDetails.getUsername());
    }

    @PutMapping("/{id}/done")
    public void sendToDone(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        issueService.sendIssueToDone(projectId, id, userDetails.getUsername());
    }

    @PutMapping("/{id}/open")
    public void sendToOpen(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        issueService.sendIssueToOpen(projectId, id, userDetails.getUsername());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<?> getIssueHistory(
            @PathVariable Long issueId) {
        return ResponseEntity.ok().body(
                issueHistoryService.getChangesByIssue(issueId));
    }
}
