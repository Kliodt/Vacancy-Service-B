package com.vacancy.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JsonHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode parseMessage(String message) {
        try {
            return objectMapper.readTree(message);
        } catch (Exception e) {
            log.warn("Invalid JSON message: {}", message);
            return null;
        }
    }

    public Long extractLong(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull()) return null;
        try {
            return valueNode.isNumber() ? valueNode.asLong() : Long.parseLong(valueNode.asText());
        } catch (Exception e) {
            log.warn("Failed to parse '{}' value: {}", field, valueNode);
            return null;
        }
    }

    public String buildNotification(String event, JsonNode node, String status, String message) {
        var out = objectMapper.createObjectNode();
        out.put("event", event);
        if (status != null) out.put("status", status);
        out.set("payload", node);
        if (message != null) out.put("message", message);
        return out.toString();
    }
}
