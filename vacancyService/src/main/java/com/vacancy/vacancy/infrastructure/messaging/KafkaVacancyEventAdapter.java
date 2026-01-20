package com.vacancy.vacancy.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.domain.port.VacancyEventPort;
import com.vacancy.vacancy.infrastructure.persistance.repository.VacancyJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Adapter - реализация порта VacancyEventPort через Kafka
 * Это мост между доменом и системой обмена сообщениями
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaVacancyEventAdapter implements VacancyEventPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final VacancyJpaRepository vacancyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publishVacancyDeleted(Long vacancyId) {
        try {
            var node = objectMapper.createObjectNode();
            node.put("vacancyId", vacancyId);
            kafkaTemplate.send("vacancy.deleted", node.toString());
        } catch (Exception e) {
            log.error("Failed to send vacancy.deleted event", e);
        }
    }

    @Override
    public void publishVacancyResponseCreated(UserVacancyResponse resp) {
        try {
            var out = objectMapper.createObjectNode();
            out.put("userId", resp.getUserId());
            out.put("vacancyId", resp.getVacancyId());
            // try to enrich with vacancy info
            vacancyRepository.findById(resp.getVacancyId()).ifPresent(vac -> {
                out.put("organizationId", vac.getOrganizationId());
                out.put("vacancyName", vac.getTitle());
            });
            kafkaTemplate.send("vacancyResponse.created", out.toString());
        } catch (Exception e) {
            log.error("Failed to serialize vacancy response for created event", e);
        }
    }

    @Override
    public void publishVacancyResponseUpdated(UserVacancyResponse resp) {
        try {
            var out = objectMapper.createObjectNode();
            out.put("userId", resp.getUserId());
            out.put("vacancyId", resp.getVacancyId());
            out.put("status", resp.getStatus().name());
            vacancyRepository.findById(resp.getVacancyId()).ifPresent(vac -> {
                out.put("organizationId", vac.getOrganizationId());
                out.put("vacancyName", vac.getTitle());
            });
            kafkaTemplate.send("vacancyResponse.updated", out.toString());
        } catch (Exception e) {
            log.error("Failed to serialize vacancy response for updated event", e);
        }
    }
}
