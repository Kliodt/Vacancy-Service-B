package com.vacancy.notification.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vacancy.notification.websocket.WSClient;
import com.vacancy.notification.websocket.WebSocketPushService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationLoginListener {

    private final WebSocketPushService webSocketPushService;
    private final JsonHelper helper;

    @KafkaListener(topics = "organization.login", groupId = "notification-service")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        JsonNode node = helper.parseMessage(message);
        if (node == null)
            return;

        Long orgId = helper.extractLong(node, "organizationId");
        if (orgId == null)
            return;

        WSClient client = new WSClient(orgId, true);
        String readable = "Организация вошла в систему";
        webSocketPushService.sendMessage(client, helper.buildNotification(topic, node, null, readable));
    }
}
