package com.vacancy.notification.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vacancy.notification.model.WSClient;
import com.vacancy.notification.websocket.WebSocketPushService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VacancyResponseCreatedListener {

    private final WebSocketPushService webSocketPushService;
    private final JsonHelper helper;

    @KafkaListener(topics = "vacancyResponse.created", groupId = "notification-service")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        JsonNode node = helper.parseMessage(message);
        if (node == null) return;

        Long orgId = helper.extractLong(node, "organizationId");
        if (orgId == null) return;

        WSClient client = new WSClient(orgId, true);
        String vacancyName = node.path("vacancyName").asText(null);
        String readable = vacancyName != null ? "Новый отклик на вакансию '" + vacancyName + "'" : "Создан новый отклик на вакансию";
        webSocketPushService.sendMessage(client, helper.buildNotification(topic, node, null, readable));
    }
}
