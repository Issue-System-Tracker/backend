package com.nsu.issue_tracker.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsu.issue_tracker.model.IssueStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardRealtimeBroadcaster {
    private static final String ISSUE_CREATED = "ISSUE_CREATED";
    private static final String ISSUE_UPDATED = "ISSUE_UPDATED";
    private static final String ISSUE_STATUS_CHANGED = "ISSUE_STATUS_CHANGED";

    private final ObjectMapper objectMapper;
    private final Map<Long, Set<WebSocketSession>> projectSessions = new ConcurrentHashMap<>();

    public void register(Long projectId, WebSocketSession session) {
        projectSessions.computeIfAbsent(projectId, ignored -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void unregister(Long projectId, WebSocketSession session) {
        Set<WebSocketSession> sessions = projectSessions.get(projectId);
        if (sessions == null) {
            return;
        }

        sessions.remove(session);
        if (sessions.isEmpty()) {
            projectSessions.remove(projectId);
        }
    }

    public void publishIssueStatusChanged(Long projectId,
                                          Long issueId,
                                          IssueStatus status,
                                          boolean hasNonStatusChanges,
                                          String changedBy) {
        broadcast(projectId, BoardRealtimeEvent.builder()
                .type(ISSUE_STATUS_CHANGED)
                .projectId(projectId)
                .issueId(issueId)
                .status(status)
                .hasNonStatusChanges(hasNonStatusChanges)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }

    public void publishIssueUpdated(Long projectId,
                                    Long issueId,
                                    IssueStatus status,
                                    String changedBy) {
        broadcast(projectId, BoardRealtimeEvent.builder()
                .type(ISSUE_UPDATED)
                .projectId(projectId)
                .issueId(issueId)
                .status(status)
                .hasNonStatusChanges(true)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }

    public void publishIssueCreated(Long projectId,
                                    Long issueId,
                                    IssueStatus status,
                                    String changedBy) {
        BoardRealtimeEvent event = BoardRealtimeEvent.builder()
                .type(ISSUE_CREATED)
                .projectId(projectId)
                .issueId(issueId)
                .status(status)
                .hasNonStatusChanges(false)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        broadcast(projectId, event);
    }

    private void broadcast(Long projectId, BoardRealtimeEvent event) {
        Set<WebSocketSession> sessions = projectSessions.get(projectId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize realtime event for project {}", projectId, e);
            return;
        }

        TextMessage message = new TextMessage(payload);
        sessions.removeIf(session -> !session.isOpen());
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                log.warn("Failed to send realtime event to session {}", session.getId(), e);
            }
        }
    }
}
