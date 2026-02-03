package com.vacancy.vacancy.controller;

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

import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.service.UserVacancyResponseService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyResponseController {

    private final UserVacancyResponseService responseService;

    @Operation(summary = "Откликнуться на вакансию")
    @PostMapping("/{vacancyId}/responses")
    public ResponseEntity<UserVacancyResponse> respondToVacancy(
            @PathVariable Long vacancyId,
            Authentication auth) {
        return ResponseEntity.ok(responseService.respondToVacancy(vacancyId, auth));
    }

    @Operation(summary = "Удалить отклик")
    @DeleteMapping("/{vacancyId}/responses")
    public ResponseEntity<Void> removeResponseFromVacancy(
            @PathVariable Long vacancyId,
            Authentication auth) {
        responseService.removeResponseFromVacancy(vacancyId, auth);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить все отклики пользователя (для пользователей)")
    @GetMapping("/responses")
    public ResponseEntity<List<UserVacancyResponse>> getAllVacancyResponsesByUser(Authentication auth) {
        return ResponseEntity.ok(responseService.getUserResponses(auth));
    }

    @Operation(summary = "Получить все отклики на вакансию (для организаций)")
    @GetMapping("/{vacancyId}/responses")
    public ResponseEntity<List<UserVacancyResponse>> getAllVacancyResponsesByVacancy(@PathVariable Long vacancyId,
            Authentication auth) {
        return ResponseEntity.ok(responseService.getVacancyResponses(vacancyId, auth));
    }

    @Operation(summary = "Изменить статус отклика на вакансию")
    @PutMapping("/responses/{responseId}")
    public ResponseEntity<UserVacancyResponse> updateVacancyResponseStatus(
            @PathVariable Long responseId,
            @RequestParam("newStatus") UserVacancyResponse.Status status,
            Authentication auth) {
        return ResponseEntity.ok(responseService.changeResponseStatus(responseId, status, auth));
    }

}
