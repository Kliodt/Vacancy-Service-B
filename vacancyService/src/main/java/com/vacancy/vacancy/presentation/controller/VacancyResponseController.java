package com.vacancy.vacancy.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.vacancy.application.usecase.ChangeResponseStatusUseCase;
import com.vacancy.vacancy.application.usecase.GetUserResponsesUseCase;
import com.vacancy.vacancy.application.usecase.GetVacancyResponsesUseCase;
import com.vacancy.vacancy.application.usecase.RemoveResponseFromVacancyUseCase;
import com.vacancy.vacancy.application.usecase.RespondToVacancyUseCase;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.UserVacancyResponse;
import com.vacancy.vacancy.infrastructure.security.AuthenticationToDomainConverter;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller - Vacancy Responses API
 * Представляет API для работы с откликами на вакансии
 */
@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyResponseController {

    private final RespondToVacancyUseCase respondToVacancyUseCase;
    private final RemoveResponseFromVacancyUseCase removeResponseFromVacancyUseCase;
    private final GetUserResponsesUseCase getUserResponsesUseCase;
    private final GetVacancyResponsesUseCase getVacancyResponsesUseCase;
    private final ChangeResponseStatusUseCase changeResponseStatusUseCase;
    
    private final AuthenticationToDomainConverter authConverter;

    @Operation(summary = "Откликнуться на вакансию")
    @PostMapping("/{vacancyId}/responses")
    public ResponseEntity<UserVacancyResponse> respondToVacancy(
            @PathVariable Long vacancyId,
            Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        return ResponseEntity.ok(respondToVacancyUseCase.execute(vacancyId, currentUser));
    }

    @Operation(summary = "Удалить отклик")
    @DeleteMapping("/{vacancyId}/responses")
    public ResponseEntity<Void> removeResponseFromVacancy(
            @PathVariable Long vacancyId,
            Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        removeResponseFromVacancyUseCase.execute(vacancyId, currentUser);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить все отклики пользователя (для пользователей)")
    @GetMapping("/responses")
    public ResponseEntity<List<UserVacancyResponse>> getAllVacancyResponsesByUser(Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        return ResponseEntity.ok(getUserResponsesUseCase.execute(currentUser));
    }

    @Operation(summary = "Получить все отклики на вакансию (для организаций)")
    @GetMapping("/{vacancyId}/responses")
    public ResponseEntity<List<UserVacancyResponse>> getAllVacancyResponsesByVacancy(@PathVariable Long vacancyId,
            Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        return ResponseEntity.ok(getVacancyResponsesUseCase.execute(vacancyId, currentUser));
    }

    @Operation(summary = "Изменить статус отклика на вакансию")
    @PutMapping("/responses/{responseId}")
    public ResponseEntity<UserVacancyResponse> updateVacancyResponseStatus(
            @PathVariable Long responseId,
            @RequestParam("newStatus") UserVacancyResponse.Status status,
            Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        return ResponseEntity.ok(changeResponseStatusUseCase.execute(responseId, status, currentUser));
    }
}
