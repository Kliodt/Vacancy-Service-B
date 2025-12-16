package com.vacancy.organization.controller;

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

import com.vacancy.organization.model.Organization;
import com.vacancy.organization.model.dto.OrganizationDtoIn;
import com.vacancy.organization.model.dto.OrganizationDtoOut;
import com.vacancy.organization.service.OrganizationService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
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
    public Mono<ResponseEntity<OrganizationDtoOut>> createOrganization(
            @RequestBody @Valid OrganizationDtoIn organization) {
        return organizationService.createOrganization(modelMapper.map(organization, Organization.class))
                .map(org -> modelMapper.map(org, OrganizationDtoOut.class))
                .map(org -> ResponseEntity.status(HttpStatus.CREATED).body(org));
    }

    @Operation(summary = "Обновить организацию")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<OrganizationDtoOut>> updateOrganization(@PathVariable Long id,
            @RequestBody @Valid OrganizationDtoIn organization) {
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

}
