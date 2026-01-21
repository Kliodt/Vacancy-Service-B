package com.vacancy.organization.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.organization.application.usecase.LoginOrganizationUseCase;
import com.vacancy.organization.presentation.dto.OrganizationRequestLoginDto;
import com.vacancy.organization.presentation.dto.AuthResponse;
import com.vacancy.organization.infrastructure.security.CustomUserDetails;
import com.vacancy.organization.infrastructure.security.JwtUtils;

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
    private final JwtUtils jwtUtils;

    @Operation(summary = "Вход организации")
    @PostMapping("/login")
    public Mono<AuthResponse> login(
            @Valid @RequestBody OrganizationRequestLoginDto request) {
        return loginOrganizationUseCase.execute(request.getEmail(), request.getPassword())
                .map(org -> {
                    String token = jwtUtils.generateToken(new CustomUserDetails(org));
                    return new AuthResponse(token);
                });
    }
}
