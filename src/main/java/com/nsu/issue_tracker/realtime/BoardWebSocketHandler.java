package com.nsu.issue_tracker.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class BoardWebSocketHandler extends TextWebSocketHandler {
    private static final String PROJECT_ID_ATTR = "projectId";

    private final BoardRealtimeBroadcaster broadcaster;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long projectId = (Long) session.getAttributes().get(PROJECT_ID_ATTR);
        if (projectId != null) {
            broadcaster.register(projectId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long projectId = (Long) session.getAttributes().get(PROJECT_ID_ATTR);
        if (projectId != null) {
            broadcaster.unregister(projectId, session);
        }
    }
}
