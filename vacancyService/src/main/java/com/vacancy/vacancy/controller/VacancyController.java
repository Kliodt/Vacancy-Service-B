package com.vacancy.vacancy.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.vacancy.model.Vacancy;
import com.vacancy.vacancy.model.dto.VacancyDtoIn;
import com.vacancy.vacancy.model.dto.VacancyDtoOut;
import com.vacancy.vacancy.service.VacancyService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;
    private final ModelMapper modelMapper;

    @Operation(summary = "Получить все вакансии")
    @GetMapping(value = "/", params = "!organizationId")
    public ResponseEntity<List<VacancyDtoOut>> getAllVacancies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<Vacancy> vacancyPage = vacancyService.getAllVacancies(page, size);
        List<VacancyDtoOut> dtoList = vacancyPage.getContent().stream()
                .map(v -> modelMapper.map(v, VacancyDtoOut.class)).toList();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(vacancyPage.getTotalElements()));

        return ResponseEntity.ok().headers(headers).body(dtoList);
    }

    @Operation(summary = "Получить вакансию по id")
    @GetMapping("/{vacancyId}")
    public ResponseEntity<VacancyDtoOut> getVacancyById(@PathVariable Long vacancyId) {
        Vacancy v = vacancyService.getVacancyById(vacancyId);
        return ResponseEntity.ok(modelMapper.map(v, VacancyDtoOut.class));
    }

    @Operation(summary = "Получить все вакансии по id организации")
    @GetMapping(value = "/", params = "organizationId")
    public ResponseEntity<List<VacancyDtoOut>> getVacancyByOrganization(@RequestParam Long organizationId) {
        List<Vacancy> vacancies = vacancyService.getVacanciesByOrganization(organizationId);
        List<VacancyDtoOut> dtoList = vacancies.stream()
                .map(v -> modelMapper.map(v, VacancyDtoOut.class)).toList();
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "Создать вакансию")
    @PostMapping
    public ResponseEntity<VacancyDtoOut> createVacancy(
            @Valid @RequestBody VacancyDtoIn vacancy,
            Authentication auth) {
        Vacancy vac = modelMapper.map(vacancy, Vacancy.class);
        Vacancy created = vacancyService.createVacancy(vac, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(created, VacancyDtoOut.class));
    }

    @Operation(summary = "Обновить вакансию")
    @PutMapping(value = "/{vacancyId}")
    public ResponseEntity<VacancyDtoOut> updateVacancy(
            @PathVariable Long vacancyId,
            @Valid @RequestBody VacancyDtoIn vacancy,
            Authentication auth) {
        Vacancy vac = modelMapper.map(vacancy, Vacancy.class);
        Vacancy updated = vacancyService.updateVacancy(vacancyId, vac, auth);
        return ResponseEntity.ok(modelMapper.map(updated, VacancyDtoOut.class));
    }

    @Operation(summary = "Удалить вакансию")
    @DeleteMapping("/{vacancyId}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long vacancyId, Authentication auth) {
        vacancyService.deleteVacancy(vacancyId, auth);
        return ResponseEntity.noContent().build();
    }

}
