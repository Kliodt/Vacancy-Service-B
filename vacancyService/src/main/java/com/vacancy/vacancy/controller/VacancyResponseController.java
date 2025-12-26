package com.vacancy.vacancy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    private Long getCurrentUserId() {
        return Long.valueOf((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Operation(summary = "Откликнуться на вакансию")
    @PutMapping
    public ResponseEntity<Void> respondToVacancy(
            @RequestParam(value = "vacancyId") Long vacancyId,
            @RequestParam(value = "userId") Long userId) {

        responseService.respondToVacancy(vacancyId, userId, getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить отклик")
    @DeleteMapping
    public ResponseEntity<Void> removeResponseFromVacancy(
            @RequestParam(value = "vacancyId") Long vacancyId,
            @RequestParam(value = "userId") Long userId) {

        responseService.removeResponseFromVacancy(vacancyId, userId, getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить ответы пользователя по userId")
    @GetMapping("/user")
    public ResponseEntity<List<UserVacancyResponse>> getAllVacancyResponsesByUser(
            @RequestParam(value = "userId") Long userId) {
        return ResponseEntity.ok(responseService.getUserResponses(userId, getCurrentUserId()));
    }

    @Operation(summary = "Получить ответы на вакансию по vacancyId")
    @GetMapping("/vacancy")
    public ResponseEntity<List<UserVacancyResponse>> getAllVacancyResponsesByVacancy(
            @RequestParam(value = "vacancyId") Long vacancyId) {
        return ResponseEntity.ok(responseService.getVacancyResponses(vacancyId));
    }

}
