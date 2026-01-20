package com.vacancy.vacancy.infrastructure.messaging;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.infrastructure.persistance.repository.UserVacancyResponseJpaRepository;
import com.vacancy.vacancy.infrastructure.persistance.repository.VacancyJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Consumer - слушает внешние события из других сервисов
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    private final UserVacancyResponseJpaRepository responseRepository;
    private final VacancyJpaRepository vacancyRepository;
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
            log.info("Received user.deleted");
            JsonNode json = objectMapper.readTree(message);
            Long userId = extractLong(json, "userId");
            List<UserVacancyResponse> list = responseRepository.findByUserId(userId);
            responseRepository.deleteAll(list);
            log.info("Deleted {} responses for user {}", list.size(), userId);
        } catch (Exception e) {
            log.error("Failed to handle user.deleted message: {}", message, e);
        }
    }

    @KafkaListener(topics = "organization.deleted", groupId = "vacancy-service")
    public void handleOrganizationDeleted(String message) {
        try {
            log.info("Received organization.deleted");
            JsonNode json = objectMapper.readTree(message);
            Long orgId = extractLong(json, "organizationId");
            var vacancies = vacancyRepository.findByOrganizationId(orgId);
            vacancyRepository.deleteAll(vacancies);
            log.info("Deleted {} vacancies for organization {}", vacancies.size(), orgId);
        } catch (Exception e) {
            log.error("Failed to handle organization.deleted message: {}", message, e);
        }
    }
}
