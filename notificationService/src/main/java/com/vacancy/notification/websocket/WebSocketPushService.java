package com.vacancy.notification.websocket;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebSocketPushService {

    private final WebSocketSessionRegistry registry;

    public void sendMessage(WSClient client, String message) {
        for (WebSocketSession session : registry.getSessions(client)) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
