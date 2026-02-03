package com.vacancy.notification.websocket;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.vacancy.notification.security.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    @SuppressWarnings("null")
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String auth = request.getHeaders().getFirst("Authorization");

        if (auth == null || !auth.startsWith("Bearer "))
            return false; // 401

        try {
            String token = auth.substring(7);
            var authObj = jwtUtils.getAuthenticationFromToken(token);
            attributes.put("auth", authObj); // pass to WebSocketSession
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("null")
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
                // nop
    }

}
