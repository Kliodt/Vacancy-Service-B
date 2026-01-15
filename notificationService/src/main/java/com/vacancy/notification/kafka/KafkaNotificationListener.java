package com.vacancy.notification.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacancy.notification.model.WSClient;
import com.vacancy.notification.websocket.WebSocketPushService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationListener {

    private final WebSocketPushService webSocketPushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = {"user.login", "organization.login", "vacancyResponse.created", "vacancyResponse.updated"})
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        JsonNode node;
        try {
            node = objectMapper.readTree(message);
        } catch (Exception e) {
            log.warn("Invalid JSON message: {}", message);
            return;
        }
        try {
            if (isUserTopic(topic)) {
                handleUserTopic(topic, node, message);
            } else if (isOrganizationTopic(topic)) {
                handleOrganizationTopic(topic, node, message);
            } else {
                log.warn("Unknown topic {} with message {}", topic, message);
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka message on topic {}: {}", topic, e.getMessage(), e);
        }
    }


    private boolean isUserTopic(String topic) {
        return "user.login".equals(topic) || "vacancyResponse.updated".equals(topic);
    }

    private boolean isOrganizationTopic(String topic) {
        return "organization.login".equals(topic) || "vacancyResponse.created".equals(topic);
    }

    private void handleUserTopic(String topic, JsonNode node, String rawMessage) {
        Long userId = extractLong(node, "userId");
        if (userId == null) {
            log.warn("Message on {} missing userId: {}", topic, rawMessage);
            return;
        }

        WSClient client = new WSClient(userId, false);

        if ("vacancyResponse.updated".equals(topic)) {
            webSocketPushService.sendMessage(client, buildUpdatedPayload(node));
        } else {
            webSocketPushService.sendMessage(client, rawMessage);
        }
    }

    private void handleOrganizationTopic(String topic, JsonNode node, String rawMessage) {
        Long orgId = extractLong(node, "organizationId");
        if (orgId == null) {
            log.warn("Message on {} missing organizationId: {}", topic, rawMessage);
            return;
        }
        WSClient client = new WSClient(orgId, true);
        webSocketPushService.sendMessage(client, rawMessage);
    }

    private Long extractLong(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode idNode = node.get(field);
        if (idNode == null || idNode.isNull()) return null;
        try {
            return idNode.isNumber() ? idNode.asLong() : Long.parseLong(idNode.asText());
        } catch (Exception e) {
            log.warn("Failed to parse {} value: {}", field, idNode);
            return null;
        }
    }

    private String buildUpdatedPayload(JsonNode node) {
        var out = objectMapper.createObjectNode();
        JsonNode statusNode = node.get("status");
        if (statusNode != null && !statusNode.isNull()) {
            out.put("status", statusNode.asText());
        }
        out.set("payload", node);
        return out.toString();
    }
}
