package com.vacancy.notification.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.vacancy.notification.security.JwtUtils;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final JwtUtils jwtUtils;
    private final WebSocketSessionRegistry sessionRegistry;
    
    @Override
    @SuppressWarnings("null")
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(new JwtWebSocketHandler(sessionRegistry), "/ws")
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtils))
                .setAllowedOriginPatterns("*");
    }
}
