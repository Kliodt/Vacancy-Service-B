package com.vacancy.notification.websocket;

import org.springframework.security.core.Authentication;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("null")
@RequiredArgsConstructor
public class JwtWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionRegistry registry;

    private WSClient authToClient(Authentication auth) {
        Long id = Long.valueOf(auth.getName());
        boolean isOrg = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ROLE_ORGANIZATION".equals(grantedAuthority.getAuthority()));
        return new WSClient(id, isOrg);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Authentication auth = (Authentication) session.getAttributes().get("auth");
        session.getAttributes().put("principal", auth);
        WSClient client = authToClient(auth);

        registry.register(client, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Authentication auth = (Authentication) session.getAttributes().get("auth");
        WSClient client = authToClient(auth);

        registry.remove(client, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // simple echo+whoami replier
        Authentication auth = (Authentication) session.getAttributes().get("principal");
        WSClient client = authToClient(auth);

        String payload = message.getPayload();

        session.sendMessage(new TextMessage("Message from " +
                (client.isOrganization() ? "Organization " : "User ") +
                client.getId() + ": " + payload));
    }
}
