package com.vacancy.vacancy.presentation.controller;

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

import com.vacancy.vacancy.application.usecase.CreateVacancyUseCase;
import com.vacancy.vacancy.application.usecase.DeleteVacancyUseCase;
import com.vacancy.vacancy.application.usecase.GetAllVacanciesUseCase;
import com.vacancy.vacancy.application.usecase.GetVacanciesByOrganizationUseCase;
import com.vacancy.vacancy.application.usecase.GetVacancyByIdUseCase;
import com.vacancy.vacancy.application.usecase.UpdateVacancyUseCase;
import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Vacancy;
import com.vacancy.vacancy.infrastructure.security.AuthenticationToDomainConverter;
import com.vacancy.vacancy.presentation.dto.VacancyRequestDto;
import com.vacancy.vacancy.presentation.dto.VacancyResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller - Vacancy API
 * Представляет API для работы с вакансиями
 */
@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final GetAllVacanciesUseCase getAllVacanciesUseCase;
    private final GetVacancyByIdUseCase getVacancyByIdUseCase;
    private final GetVacanciesByOrganizationUseCase getVacanciesByOrganizationUseCase;
    private final CreateVacancyUseCase createVacancyUseCase;
    private final UpdateVacancyUseCase updateVacancyUseCase;
    private final DeleteVacancyUseCase deleteVacancyUseCase;
    
    private final ModelMapper modelMapper;
    private final AuthenticationToDomainConverter authConverter;

    @Operation(summary = "Получить все вакансии")
    @GetMapping(value = "/", params = "!organizationId")
    public ResponseEntity<List<VacancyResponseDto>> getAllVacancies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<Vacancy> vacancyPage = getAllVacanciesUseCase.execute(page, size);
        List<VacancyResponseDto> dtoList = vacancyPage.getContent().stream()
                .map(v -> modelMapper.map(v, VacancyResponseDto.class)).toList();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(vacancyPage.getTotalElements()));

        return ResponseEntity.ok().headers(headers).body(dtoList);
    }

    @Operation(summary = "Получить вакансию по id")
    @GetMapping("/{vacancyId}")
    public ResponseEntity<VacancyResponseDto> getVacancyById(@PathVariable Long vacancyId) {
        Vacancy v = getVacancyByIdUseCase.execute(vacancyId);
        return ResponseEntity.ok(modelMapper.map(v, VacancyResponseDto.class));
    }

    @Operation(summary = "Получить все вакансии по id организации")
    @GetMapping(value = "/", params = "organizationId")
    public ResponseEntity<List<VacancyResponseDto>> getVacancyByOrganization(@RequestParam Long organizationId) {
        List<Vacancy> vacancies = getVacanciesByOrganizationUseCase.execute(organizationId);
        List<VacancyResponseDto> dtoList = vacancies.stream()
                .map(v -> modelMapper.map(v, VacancyResponseDto.class)).toList();
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "Создать вакансию")
    @PostMapping
    public ResponseEntity<VacancyResponseDto> createVacancy(
            @Valid @RequestBody VacancyRequestDto vacancy,
            Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        Vacancy vac = modelMapper.map(vacancy, Vacancy.class);
        Vacancy created = createVacancyUseCase.execute(vac, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(created, VacancyResponseDto.class));
    }

    @Operation(summary = "Обновить вакансию")
    @PutMapping(value = "/{vacancyId}")
    public ResponseEntity<VacancyResponseDto> updateVacancy(
            @PathVariable Long vacancyId,
            @Valid @RequestBody VacancyRequestDto vacancy,
            Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        Vacancy vac = modelMapper.map(vacancy, Vacancy.class);
        Vacancy updated = updateVacancyUseCase.execute(vacancyId, vac, currentUser);
        return ResponseEntity.ok(modelMapper.map(updated, VacancyResponseDto.class));
    }

    @Operation(summary = "Удалить вакансию")
    @DeleteMapping("/{vacancyId}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long vacancyId, Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        deleteVacancyUseCase.execute(vacancyId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
