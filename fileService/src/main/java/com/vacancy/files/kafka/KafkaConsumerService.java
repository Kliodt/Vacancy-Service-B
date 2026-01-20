package com.vacancy.files.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacancy.files.service.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final FileService fileService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long extractLong(JsonNode node, String field) {
        if (node == null)
            throw new RuntimeException("Node is null");
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull())
            throw new RuntimeException("Can't parse this field");
        try {
            return valueNode.isNumber() ? valueNode.asLong() : Long.parseLong(valueNode.asText());
        } catch (Exception e) {
            throw new RuntimeException("Can't parse this field");
        }
    }

    @KafkaListener(topics = "user.deleted", groupId = "vacancy-service")
    public void handleUserDeleted(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            Long userId = extractLong(json, "userId");
            fileService.deleteAllByUser(userId);
            log.info("Received user.deleted for userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to handle user.deleted message: {}", message, e);
        }
    }

}
