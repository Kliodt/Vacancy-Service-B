package com.vacancy.vacancy.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.repository.VacancyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final VacancyRepository vacancyRepository;
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
            var out = objectMapper.createObjectNode();
            out.put("userId", resp.getUserId());
            out.put("vacancyId", resp.getVacancyId());
            // try to enrich with vacancy info
            Vacancy vac = vacancyRepository.findById(resp.getVacancyId()).orElse(null);
            if (vac != null) {
                out.put("organizationId", vac.getOrganizationId());
                out.put("vacancyName", vac.getTitle());
            }
            kafkaTemplate.send("vacancyResponse.created", out.toString());
        } catch (Exception e) {
            log.error("Failed to serialize vacancy response for created event", e);
        }
    }

    public void sendVacancyResponseUpdated(UserVacancyResponse resp) {
        try {
            var out = objectMapper.createObjectNode();
            out.put("userId", resp.getUserId());
            out.put("vacancyId", resp.getVacancyId());
            out.put("status", resp.getStatus().name());
            Vacancy vac = vacancyRepository.findById(resp.getVacancyId()).orElse(null);
            if (vac != null) {
                out.put("organizationId", vac.getOrganizationId());
                out.put("vacancyName", vac.getTitle());
            }
            kafkaTemplate.send("vacancyResponse.updated", out.toString());
        } catch (Exception e) {
            log.error("Failed to serialize vacancy response for updated event", e);
        }
    }

}
