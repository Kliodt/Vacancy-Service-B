package com.vacancy.organization.presentation.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.organization.application.usecase.CreateOrganizationUseCase;
import com.vacancy.organization.application.usecase.LoginOrganizationUseCase;
import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Organization;
import com.vacancy.organization.infrastructure.security.AuthenticationToDomainConverter;
import com.vacancy.organization.presentation.dto.OrganizationRequestCreateDto;
import com.vacancy.organization.presentation.dto.OrganizationRequestLoginDto;
import com.vacancy.organization.presentation.dto.OrganizationResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST Controller - Authentication API
 * Использует use cases для регистрации и логина организаций
 */
@RestController
@RequestMapping("/auth/organization")
@RequiredArgsConstructor
public class AuthController {

    private final CreateOrganizationUseCase createOrganizationUseCase;
    private final LoginOrganizationUseCase loginOrganizationUseCase;
    private final ModelMapper modelMapper;
    private final AuthenticationToDomainConverter authConverter;

    @Operation(summary = "Зарегистрировать новую организацию")
    @PostMapping("/register")
    public Mono<ResponseEntity<OrganizationResponseDto>> register(
            @Valid @RequestBody OrganizationRequestCreateDto request,
            Authentication auth) {
        CurrentUser currentUser = authConverter.convert(auth);
        Organization organization = modelMapper.map(request, Organization.class);
        return createOrganizationUseCase.execute(organization, currentUser)
                .map(org -> modelMapper.map(org, OrganizationResponseDto.class))
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Operation(summary = "Вход организации")
    @PostMapping("/login")
    public Mono<ResponseEntity<OrganizationResponseDto>> login(
            @Valid @RequestBody OrganizationRequestLoginDto request) {
        return loginOrganizationUseCase.execute(request.getEmail(), request.getPassword())
                .map(org -> modelMapper.map(org, OrganizationResponseDto.class))
                .map(ResponseEntity::ok);
    }
}
