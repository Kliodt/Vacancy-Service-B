package com.vacancy.vacancy.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.vacancy.vacancy.repository.UserVacancyResponseRepository;
import com.vacancy.vacancy.repository.VacancyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final UserVacancyResponseRepository responseRepository;
    private final VacancyRepository vacancyRepository;

    @KafkaListener(topics = "user.deleted", groupId = "vacancy-service")
    public void handleUserDeleted(String message) {
        try {
            Long userId = Long.parseLong(message.trim());
            log.info("Received user.deleted for userId={}", userId);
            var list = responseRepository.findByUserId(userId);
            responseRepository.deleteAll(list);
            log.info("Deleted {} responses for user {}", list.size(), userId);
        } catch (Exception e) {
            log.error("Failed to handle user.deleted message: {}", message, e);
        }
    }

    @KafkaListener(topics = "organization.deleted", groupId = "vacancy-service")
    public void handleOrganizationDeleted(String message) {
        try {
            Long orgId = Long.parseLong(message.trim());
            log.info("Received organization.deleted for orgId={}", orgId);
            var vacancies = vacancyRepository.findByOrganizationId(orgId);
            vacancyRepository.deleteAll(vacancies);
            log.info("Deleted {} vacancies for organization {}", vacancies.size(), orgId);
        } catch (Exception e) {
            log.error("Failed to handle organization.deleted message: {}", message, e);
        }
    }

}
