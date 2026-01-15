package com.vacancy.vacancy.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacancy.vacancy.model.UserVacancyResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendVacancyDeleted(Long vacancyId) {
        try {
            var node = objectMapper.createObjectNode();
            node.put("vacancyId", vacancyId);
            kafkaTemplate.send("vacancy.deleted", node.toString());
        } catch (Exception e) {
            log.error("Failed to send vacancy.deleted event", e);
        }
    }

    public void sendVacancyResponseCreated(UserVacancyResponse resp) {
        try {
            String payload = objectMapper.writeValueAsString(resp);
            kafkaTemplate.send("vacancyResponse.created", payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize vacancy response for created event", e);
        }
    }

    public void sendVacancyResponseUpdated(UserVacancyResponse resp) {
        try {
            String payload = objectMapper.writeValueAsString(resp);
            kafkaTemplate.send("vacancyResponse.updated", payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize vacancy response for updated event", e);
        }
    }

}
