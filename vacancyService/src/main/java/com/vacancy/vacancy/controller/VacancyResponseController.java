package com.vacancy.vacancy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.service.VacancyService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.QueryParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vacancies/responses")
@RequiredArgsConstructor
public class VacancyResponseController {

    private final VacancyService vacancyService;

    @Operation(summary = "Откликнуться на вакансию")
    @PutMapping
    public ResponseEntity<Void> respondToVacancy(
            @QueryParam("vacancyId") Long vacancyId,
            @QueryParam("userId") Long userId) {

        if (vacancyId == null || userId == null)
            return ResponseEntity.badRequest().build();

        vacancyService.respondToVacancy(vacancyId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить отклик")
    @DeleteMapping
    public ResponseEntity<Void> removeResponseFromVacancy(
            @QueryParam("vacancyId") Long vacancyId,
            @QueryParam("userId") Long userId) {

        if (vacancyId == null || userId == null)
            return ResponseEntity.badRequest().build();

        vacancyService.removeResponseFromVacancy(vacancyId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить ответы по userId или vacancyId")
    @GetMapping
    public ResponseEntity<List<UserVacancyResponse>> getAllVacancyResponses(
            @QueryParam("vacancyId") Long vacancyId,
            @QueryParam("userId") Long userId) {

        if (vacancyId != null && userId != null) {
            return ResponseEntity.ok(vacancyService.getVacancyResponsesForUser(userId, vacancyId));
        } else if (vacancyId != null) {
            return ResponseEntity.ok(vacancyService.getVacancyResponses(vacancyId));
        } else if (userId != null) {
            return ResponseEntity.ok(vacancyService.getUserResponses(userId));
        }

        return ResponseEntity.badRequest().build();
    }

}
