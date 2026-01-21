package com.vacancy.user.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;

import com.vacancy.user.presentation.dto.LoginRequestDto;
import com.vacancy.user.presentation.dto.AuthResponse;
import com.vacancy.user.application.usecase.LoginUserUseCase;
import com.vacancy.user.infrastructure.security.CustomUserDetails;
import com.vacancy.user.infrastructure.security.JwtUtils;

@RestController
@RequestMapping("/auth/user")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUserUseCase loginUserUseCase;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public Mono<AuthResponse> login(@RequestBody LoginRequestDto request) {
        return loginUserUseCase.execute(request.getEmail(), request.getPassword())
            .map(user -> {
                String token = jwtUtils.generateToken(new CustomUserDetails(user));
                return new AuthResponse(token);
            });
    }
}
