package com.vacancy.notification.websocket;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketSessionRegistry {

    private final Map<WSClient, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void register(WSClient client, WebSocketSession session) {
        sessions.computeIfAbsent(client, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void remove(WSClient client, WebSocketSession session) {
        var set = sessions.get(client);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) sessions.remove(client);
        }
    }

    public Set<WebSocketSession> getSessions(WSClient client) {
        return sessions.getOrDefault(client, Set.of());
    }
}
