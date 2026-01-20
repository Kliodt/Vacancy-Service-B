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
public class VacancyResponseUpdatedListener {

    private final WebSocketPushService webSocketPushService;
    private final JsonHelper helper;

    @KafkaListener(topics = "vacancyResponse.updated", groupId = "notification-service")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        JsonNode node = helper.parseMessage(message);
        if (node == null) return;

        Long userId = helper.extractLong(node, "userId");
        if (userId == null) return;

        WSClient client = new WSClient(userId, false);
        String vacancyName = node.path("vacancyName").asText(null);
        String status = node.path("status").asText(null);
        String readable;
        if (vacancyName != null && status != null) {
            readable = "Статус отклика на вакансию '" + vacancyName + "' изменён на " + status;
        } else if (vacancyName != null) {
            readable = "Отклик на вакансию '" + vacancyName + "' обновлён";
        } else {
            readable = "Отклик на вакансию обновлён";
        }
        webSocketPushService.sendMessage(client, helper.buildNotification(topic, node, status, readable));
    }
}
