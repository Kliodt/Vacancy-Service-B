package com.vacancy.vacancy.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.model.UserVacancyResponse;
import com.vacancy.vacancy.service.VacancyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;

    @Operation(
            summary = "Получить все вакансии",
            description = "Возвращает список вакансий страницами"
    )
    @GetMapping
    public ResponseEntity<List<Vacancy>> getAllVacancies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Page<Vacancy> vacancyPage = vacancyService.getAllVacancies(page, size);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(vacancyPage.getTotalElements()));
        
        return ResponseEntity.ok().headers(headers).body(vacancyPage.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacancy> getVacancyById(@PathVariable Long id) {
        return ResponseEntity.ok(vacancyService.getVacancyById(id));
    }

    @PostMapping
    public ResponseEntity<Vacancy> createVacancy(@Valid @RequestBody Vacancy vacancy) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyService.saveVacancy(vacancy));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vacancy> updateVacancy(
            @PathVariable Long id,
            @Valid @RequestBody Vacancy vacancy) {
        vacancy.setId(id);
        return ResponseEntity.ok(vacancyService.saveVacancy(vacancy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long id) {
        vacancyService.deleteVacancy(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{vacancyId}/respond/{userId}")
    public ResponseEntity<Void> respondToVacancy(
            @PathVariable Long vacancyId,
            @PathVariable Long userId) {
        vacancyService.respondToVacancy(vacancyId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{vacancyId}/respond/{userId}")
    public ResponseEntity<Void> removeResponseFromVacancy(
            @PathVariable Long vacancyId,
            @PathVariable Long userId) {
        vacancyService.removeResponseFromVacancy(vacancyId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{vacancyId}/responses")
    public ResponseEntity<List<UserVacancyResponse>> getVacancyResponses(
            @PathVariable Long vacancyId) {
        return ResponseEntity.ok(vacancyService.getVacancyResponses(vacancyId));
    }
}
