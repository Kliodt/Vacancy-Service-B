package com.vacancy.vacancy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.service.UserVacancyResponseService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vacancies/responses")
@RequiredArgsConstructor
public class VacancyResponseController {

    private final UserVacancyResponseService responseService;

    @Operation(summary = "Откликнуться на вакансию")
    @PostMapping
    public ResponseEntity<UserVacancyResponse> respondToVacancy(
            @RequestParam(value = "vacancyId") Long vacancyId,
            @RequestParam(value = "userId") Long userId) {
        return ResponseEntity.ok(responseService.respondToVacancy(vacancyId, userId));
    }

    @Operation(summary = "Удалить отклик")
    @DeleteMapping
    public ResponseEntity<Void> removeResponseFromVacancy(
            @RequestParam(value = "vacancyId") Long vacancyId,
            @RequestParam(value = "userId") Long userId) {

        responseService.removeResponseFromVacancy(vacancyId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить все отклики пользователя (для пользователей)")
    @GetMapping("/user")
    public ResponseEntity<List<UserVacancyResponse>> getAllVacancyResponsesByUser(
            @RequestParam(value = "userId") Long userId) {
        return ResponseEntity.ok(responseService.getUserResponses(userId));
    }

    @Operation(summary = "Получить все отклики на вакансию (для организаций)")
    @GetMapping("/vacancy")
    public ResponseEntity<List<UserVacancyResponse>> getAllVacancyResponsesByVacancy(
            @RequestParam(value = "vacancyId") Long vacancyId,
            @AuthenticationPrincipal Long organizationId) {
        return ResponseEntity.ok(responseService.getVacancyResponses(organizationId, vacancyId));
    }

    @Operation(summary = "Изменить статус отклика на вакансию")
    @GetMapping("/{responseId}")
    public ResponseEntity<UserVacancyResponse> changeVacancyResponseStatus(
            @PathVariable Long responseId,
            @RequestParam("newStatus") UserVacancyResponse.Status status,
            @AuthenticationPrincipal Long organizationId) {
        return ResponseEntity.ok(responseService.changeResponseStatus(responseId, organizationId, status));
    }

}
