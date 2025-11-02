package com.nsu.issue_tracker.service;

import com.nsu.issue_tracker.dto.CreatingIssueRequest;
import com.nsu.issue_tracker.dto.EditingIssueRequest;
import com.nsu.issue_tracker.dto.FilteringIssuesRequest;
import com.nsu.issue_tracker.dto.IssueResponse;
import com.nsu.issue_tracker.model.Issue;
import com.nsu.issue_tracker.model.IssueStatus;
import com.nsu.issue_tracker.model.Project;
import com.nsu.issue_tracker.model.User;
import com.nsu.issue_tracker.repository.IssueRepository;
import com.nsu.issue_tracker.service.mappers.IssueToIssueDtoMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final UserService userService;
    private final SprintService sprintService;
    private final IssueHistoryService issueHistoryService;
    private final ProjectService projectService;
    private final IssueToIssueDtoMapper issueDtoMapper;

    public void createIssue(
            CreatingIssueRequest request, Long projectId, UUID authorUUID) {

        Project project = projectService.findById(projectId);

        User author = userService.findById(authorUUID);

        if (!project.getMembers().contains(author))
            throw new AccessDeniedException
                    ("You can create issues only if you are member of the project");


        save(
                Issue.builder()
                        .project(project)
                        .author(author)
                        .title(request.title())
                        .description(request.description())
                        .status(IssueStatus.OPEN)
                        .priority(request.priority())
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .sprint(sprintService.getReference(request.sprintId()))
                        .build()

        );
    }

    public List<IssueResponse> filterIssues(Long projectId, FilteringIssuesRequest request) {
        User assignee = request.assigneeEmail() != null
                ? userService.findByEmail(request.assigneeEmail())
                : null;

        return issueRepository.findFilteredIssues(
                projectId,
                request.status(),
                assignee,
                request.sprintId()
        ).stream().map(issueDtoMapper).toList();
    }

    public void deleteIssue(Long projectId, Long issueId, UUID userUUID) {
        Issue issue = findById(issueId);

        Project project = projectService.findById(projectId);

        if (!issue.getProject().equals(project))
            throw new AccessDeniedException
                    ("You can't delete issue from another project within current project");

        if (!(project.getAdmin().getId().equals(userUUID)
                || issue.getAuthor().getId().equals(userUUID)))
            throw new AccessDeniedException
                    ("Only project's admin / issue's author can delete issue");

        issueRepository.delete(issue);
    }

    @Transactional
    public void editIssue(
            EditingIssueRequest request,
            Long issueId,
            UUID userId,
            Long projectId) {

        User assignee = null;
        if (request.getAssigneeEmail() != null)
            assignee = userService.findByEmail(request.getAssigneeEmail());

        User user = userService.findById(userId);

        Project project = projectService.findById(projectId);

        Issue issue = findById(issueId);

        if (!issue.getProject().equals(project))
            throw new AccessDeniedException
                    ("You can't edit issue from another project within current project");

        if (!isAdmin(project, user) && issue.getStatus().equals(IssueStatus.DONE)
                && !request.getStatus().equals(IssueStatus.DONE)
        || !isAdmin(project, user) && request.getStatus().equals(IssueStatus.DONE)
                && !issue.getStatus().equals(IssueStatus.DONE))
            throw new AccessDeniedException
                    ("Only admin can change status to Done and vice versa");
        if (!isAdmin(project, user) && !issue.getAuthor().getEmail().equals(user.getEmail()))
            throw new AccessDeniedException("You cant refactor other people's issues");

        issueHistoryService.recordChanges(request, issue, user.getEmail());

        save(Issue.builder()
                .id(issueId)
                .title(request.getTitle())
                .status(request.getStatus())
                .description(request.getDescription())
                .author(issue.getAuthor())
                .assignee(assignee)
                .type(request.getType())
                .priority(request.getPriority())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .sprint(sprintService.getReference(request.getSprintId()))
                .build());
    }

    @Transactional
    public void sendIssueToTesting(Long projectId, Long issueId, String userEmail) {
        Issue issue = findById(issueId);

        User user = userService.findByEmail(userEmail);

        Project project = projectService.findById(projectId);

        if (!issue.getProject().equals(project))
            throw new AccessDeniedException
                    ("You can't change status of issue from another project within current project");

        if (!project.getMembers().contains(user))
            throw new AccessDeniedException("You are not member of the project");

        if (!isAdmin(project, user) && issue.getStatus().equals(IssueStatus.DONE))
            throw new AccessDeniedException
                    ("Only admin can change issue's status to Testing from Done");

        issueHistoryService.compareAndRecord(
                issue, userEmail, "Status", issue.getStatus(), "Testing");

        if (issue.getStatus().equals(IssueStatus.OPEN))
            issue.setAssignee(user);

        issue.setStatus(IssueStatus.TESTING);
        save(issue);
    }

    @Transactional
    public void sendIssueToProgress(Long projectId, Long issueId, String userEmail) {
        Issue issue = findById(issueId);

        User user = userService.findByEmail(userEmail);

        Project project = projectService.findById(projectId);

        if (!issue.getProject().equals(project))
            throw new AccessDeniedException
                    ("You can't change status of  issue from another project within current project");

        if (!project.getMembers().contains(user))
            throw new AccessDeniedException("You are not member of the project");

        if (!isAdmin(project, user) && (issue.getStatus().equals(IssueStatus.TESTING)
                || issue.getStatus().equals(IssueStatus.DONE)))
            throw new AccessDeniedException
                    ("Only admin can change issue's status to InProgress from Testing or Done");

        issueHistoryService.compareAndRecord(
                issue, userEmail, "Status", issue.getStatus(), "InProgress");

        if (issue.getStatus().equals(IssueStatus.OPEN))
            issue.setAssignee(user);

        issue.setStatus(IssueStatus.IN_PROGRESS);
        save(issue);
    }

    @Transactional
    public void sendIssueToDone(Long projectId, Long issueId, String userEmail) {
        Issue issue = findById(issueId);

        User user = userService.findByEmail(userEmail);

        Project project = projectService.findById(projectId);

        if (!issue.getProject().equals(project))
            throw new AccessDeniedException
                    ("You can't change status of  issue from another project within current project");

        if (!isAdmin(project, user))
            throw new AccessDeniedException
                    ("Only admin can change issue's status to Done");

        issueHistoryService.compareAndRecord(
                issue, userEmail, "Status", issue.getStatus(), "Done");

        issue.setStatus(IssueStatus.DONE);
        save(issue);
    }

    @Transactional
    public void sendIssueToOpen(Long projectId, Long issueId, String userEmail) {
        Issue issue = findById(issueId);

        User user = userService.findByEmail(userEmail);

        Project project = projectService.findById(projectId);

        if (!issue.getProject().equals(project))
            throw new AccessDeniedException
                    ("You can't change status of  issue from another project within current project");

        if (!isAdmin(project, user))
            throw new AccessDeniedException
                    ("Only admin can change issue's status to Open");

        issueHistoryService.compareAndRecord(
                issue, userEmail, "Status", issue.getStatus(), "Open");

        issue.setStatus(IssueStatus.OPEN);
        issue.setAssignee(null);
        save(issue);
    }

    public List<IssueResponse> getAllIssues(Long projectId) {
        return issueRepository
                .findAllByProject(projectService.getReference(projectId))
                .stream().map(issueDtoMapper).toList();
    }

    public List<IssueResponse> getIssuesBySprint(Long projectId, Long sprintId) {
        return issueRepository
                .findAllBySprintAndProject(
                        sprintService.getReference(sprintId),
                        projectService.getReference(projectId))
                .stream().map(issueDtoMapper).toList();
    }

    public Issue findById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException
                        ("Issue with provided id does not exists"));
    }

    public void save(Issue issue) {
        issueRepository.save(issue);
    }

    public Issue getReference(Long id) {
        return issueRepository.getReferenceById(id);
    }

    private boolean isAdmin(Project project, User user) {
        return project.getAdmin().getId().equals(user.getId());
    }

}
