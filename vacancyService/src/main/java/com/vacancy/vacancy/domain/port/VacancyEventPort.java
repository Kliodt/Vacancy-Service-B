package com.vacancy.vacancy.domain.port;

import com.vacancy.vacancy.domain.model.UserVacancyResponse;

// для Kafka
public interface VacancyEventPort {
    void publishVacancyDeleted(Long vacancyId);
    void publishVacancyResponseCreated(UserVacancyResponse response);
    void publishVacancyResponseUpdated(UserVacancyResponse response);
}
