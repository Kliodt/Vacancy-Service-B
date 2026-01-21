package com.vacancy.organization.presentation.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.organization.application.usecase.LoginOrganizationUseCase;
import com.vacancy.organization.presentation.dto.OrganizationRequestLoginDto;
import com.vacancy.organization.presentation.dto.OrganizationResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST Controller - Authentication API
 */
@RestController
@RequestMapping("/auth/organization")
@RequiredArgsConstructor
public class AuthController {

    private final LoginOrganizationUseCase loginOrganizationUseCase;
    private final ModelMapper modelMapper;

    @Operation(summary = "Вход организации")
    @PostMapping("/login")
    public Mono<ResponseEntity<OrganizationResponseDto>> login(
            @Valid @RequestBody OrganizationRequestLoginDto request) {
        return loginOrganizationUseCase.execute(request.getEmail(), request.getPassword())
                .map(org -> modelMapper.map(org, OrganizationResponseDto.class))
                .map(ResponseEntity::ok);
    }
}
