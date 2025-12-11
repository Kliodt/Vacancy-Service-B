package com.vacancy.organization.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.organization.client.VacancyClient;
import com.vacancy.organization.exceptions.ServiceException;
import com.vacancy.organization.model.Organization;
import com.vacancy.organization.model.dto.OrganizationDtoIn;
import com.vacancy.organization.model.dto.OrganizationDtoOut;
import com.vacancy.organization.service.OrganizationService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final VacancyClient vacancyClient;
    private final ModelMapper modelMapper = new ModelMapper();

    @Operation(summary = "Получить все организации")
    @GetMapping
    public Flux<OrganizationDtoOut> getAllOrganizations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return organizationService.getAllOrganizations(page, size)
                .map(org -> modelMapper.map(org, OrganizationDtoOut.class));
    }

    @Operation(summary = "Получить организацию по id")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<OrganizationDtoOut>> getOrganizationById(@PathVariable Long id) {
        return organizationService.getOrganizationById(id)
                .map(org -> modelMapper.map(org, OrganizationDtoOut.class))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Создать организацию")
    @PostMapping
    public Mono<ResponseEntity<OrganizationDtoOut>> createOrganization(@Valid @RequestBody OrganizationDtoIn organization) {
        return organizationService.createOrganization(modelMapper.map(organization, Organization.class))
                .map(org -> modelMapper.map(org, OrganizationDtoOut.class))
                .map(org -> ResponseEntity.status(HttpStatus.CREATED).body(org));
    }

    @Operation(summary = "Обновить организацию")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<OrganizationDtoOut>> updateOrganization(@PathVariable Long id, @Valid @RequestBody OrganizationDtoIn organization) {
        return organizationService.updateOrganization(id, modelMapper.map(organization, Organization.class))
                .map(org -> modelMapper.map(org, OrganizationDtoOut.class))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Удалить организацию")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteOrganization(@PathVariable Long id) {
        return organizationService.deleteOrganization(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Получить вакансии, размещенные организацией")
    @GetMapping("/{orgId}/vacancies")
    @CircuitBreaker(name = "vacancyService", fallbackMethod = "getOrganizationVacanciesFallback")
    public Mono<ResponseEntity<List<Object>>> getOrganizationVacancies(@PathVariable Long orgId) {
        return organizationService.getOrganizationVacancyIds(orgId)
                .flatMapMany(Flux::fromIterable) // get organization vacancies ids
                .flatMap(id -> Mono.fromCallable(() -> vacancyClient.getVacancyById(id)).subscribeOn(Schedulers.boundedElastic()))
                .collectList()
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<List<Object>>> getOrganizationVacanciesFallback() {
        return Mono.error(new ServiceException("Сервис вакансий недоступен"));
    }

    @Operation(summary = "Разместить вакансию")
    @PostMapping("/{orgId}/vacancies/{vacancyId}")
    public Mono<ResponseEntity<Void>> addVacancyToOrganization(
            @PathVariable Long orgId,
            @PathVariable Long vacancyId) {
        return organizationService.addVacancyToOrganization(orgId, vacancyId)
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
    }

    @Operation(summary = "Убрать размещенную вакансию")
    @DeleteMapping("/{orgId}/vacancies/{vacancyId}")
    public Mono<ResponseEntity<Void>> deleteOrganizationVacancy(
            @PathVariable Long orgId,
            @PathVariable Long vacancyId) {
        return organizationService.deleteOrganizationVacancy(orgId, vacancyId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
