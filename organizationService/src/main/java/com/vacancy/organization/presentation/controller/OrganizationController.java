package com.vacancy.organization.presentation.controller;

import org.modelmapper.ModelMapper;
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

import com.vacancy.organization.application.usecase.CreateOrganizationUseCase;
import com.vacancy.organization.application.usecase.DeleteOrganizationUseCase;
import com.vacancy.organization.application.usecase.GetAllOrganizationsUseCase;
import com.vacancy.organization.application.usecase.GetOrganizationByIdUseCase;
import com.vacancy.organization.application.usecase.UpdateOrganizationUseCase;
import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.infrastructure.security.AuthenticationToDomainConverter;
import com.vacancy.organization.presentation.dto.OrganizationRequestCreateDto;
import com.vacancy.organization.presentation.dto.OrganizationRequestUpdateDto;
import com.vacancy.organization.presentation.dto.OrganizationResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller - Organization API
 * Использует use cases вместо сервисов
 */
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final GetAllOrganizationsUseCase getAllOrganizationsUseCase;
    private final GetOrganizationByIdUseCase getOrganizationByIdUseCase;
    private final CreateOrganizationUseCase createOrganizationUseCase;
    private final UpdateOrganizationUseCase updateOrganizationUseCase;
    private final DeleteOrganizationUseCase deleteOrganizationUseCase;

    private final ModelMapper modelMapper;
    private final AuthenticationToDomainConverter authConverter;

    @Operation(summary = "Получить все организации")
    @GetMapping
    public Flux<OrganizationResponseDto> getAllOrganizations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return getAllOrganizationsUseCase.execute(page, size)
                .map(org -> modelMapper.map(org, OrganizationResponseDto.class));
    }

    @Operation(summary = "Получить организацию по id")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<OrganizationResponseDto>> getOrganizationById(@PathVariable Long id) {
        return getOrganizationByIdUseCase.execute(id)
                .map(org -> modelMapper.map(org, OrganizationResponseDto.class))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Создать новую организацию")
    @PostMapping
    public Mono<ResponseEntity<OrganizationResponseDto>> createOrganization(
            @Valid @RequestBody OrganizationRequestCreateDto request,
            Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        Organization organization = modelMapper.map(request, Organization.class);
        return createOrganizationUseCase.execute(organization, currentUser)
                .map(org -> modelMapper.map(org, OrganizationResponseDto.class))
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Operation(summary = "Обновить организацию")
    @PutMapping("/{id}")
    public Mono<ResponseEntity<OrganizationResponseDto>> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequestUpdateDto request,
            Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        Organization updated = modelMapper.map(request, Organization.class);
        return updateOrganizationUseCase.execute(id, updated, currentUser)
                .map(org -> modelMapper.map(org, OrganizationResponseDto.class))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Удалить организацию")
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteOrganization(
            @PathVariable Long id,
            Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        return deleteOrganizationUseCase.execute(id, currentUser)
                .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).<Void>build()));
    }
}
