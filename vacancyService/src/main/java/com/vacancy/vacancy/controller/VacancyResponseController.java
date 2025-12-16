package com.vacancy.vacancy.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.vacancy.exceptions.RequestException;
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
    @PutMapping
    public ResponseEntity<Void> respondToVacancy(
            @RequestParam(value = "vacancyId") Long vacancyId,
            @RequestParam(value = "userId") Long userId) {

        responseService.respondToVacancy(vacancyId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить отклик")
    @DeleteMapping
    public ResponseEntity<Void> removeResponseFromVacancy(
            @RequestParam(value = "vacancyId") Long vacancyId,
            @RequestParam(value = "userId") Long userId) {

        responseService.removeResponseFromVacancy(vacancyId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить ответы по userId или vacancyId")
    @GetMapping
    public ResponseEntity<List<UserVacancyResponse>> getAllVacancyResponses(
            @RequestParam(value = "vacancyId", required = false) Long vacancyId,
            @RequestParam(value = "userId", required = false) Long userId) {

        if (vacancyId != null && userId != null) {
            return ResponseEntity.ok(responseService.getVacancyResponsesForUser(userId, vacancyId));
        } else if (vacancyId != null) {
            return ResponseEntity.ok(responseService.getVacancyResponses(vacancyId));
        } else if (userId != null) {
            return ResponseEntity.ok(responseService.getUserResponses(userId));
        }

        throw new RequestException(HttpStatus.BAD_REQUEST,
                "Необходим хотя бы один из параметров: vacancyId и userId");
    }

}
