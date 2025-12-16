package com.vacancy.vacancy.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.model.dto.VacancyDtoIn;
import com.vacancy.vacancy.service.VacancyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;
    private final ModelMapper modelMapper;

    @Operation(summary = "Получить все вакансии")
    @GetMapping
    public ResponseEntity<List<Vacancy>> getAllVacancies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<Vacancy> vacancyPage = vacancyService.getAllVacancies(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(vacancyPage.getTotalElements()));

        return ResponseEntity.ok().headers(headers).body(vacancyPage.getContent());
    }

    @Operation(summary = "Получить вакансию по id")
    @GetMapping("/{vacancyId}")
    public ResponseEntity<Vacancy> getVacancyById(@PathVariable Long vacancyId) {
        return ResponseEntity.ok(vacancyService.getVacancyById(vacancyId));
    }

    @Operation(summary = "Получить все вакансии по id организации")
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<Vacancy>> getVacancyByOrganization(@PathVariable Long organizationId) {
        return ResponseEntity.ok(vacancyService.getVacanciesByOrganization(organizationId));
    }

    @Operation(summary = "Создать вакансию")
    @PostMapping
    public ResponseEntity<Vacancy> createVacancy(@Valid @RequestBody VacancyDtoIn vacancy) {
        Vacancy vac = modelMapper.map(vacancy, Vacancy.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyService.createVacancy(vac));
    }

    @Operation(summary = "Обновить вакансию")
    @PutMapping("/{vacancyId}")
    public ResponseEntity<Vacancy> updateVacancy(
            @PathVariable Long vacancyId,
            @Valid @RequestBody VacancyDtoIn vacancy) {
        Vacancy vac = modelMapper.map(vacancy, Vacancy.class);
        return ResponseEntity.ok(vacancyService.updateVacancy(vacancyId, vac));
    }

    @Operation(summary = "Удалить вакансию")
    @DeleteMapping("/{vacancyId}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long vacancyId) {
        vacancyService.deleteVacancy(vacancyId);
        return ResponseEntity.noContent().build();
    }

}
