package com.vacancy.organization.controller;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.organization.kafka.KafkaProducerService;
import com.vacancy.organization.model.dto.AuthRequest;
import com.vacancy.organization.model.dto.AuthResponse;
import com.vacancy.organization.security.JwtUtils;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth/organization")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final ReactiveAuthenticationManager authenticationManager;
    private final KafkaProducerService kafkaProducer;

    @PostMapping("/login")
    public Mono<AuthResponse> login(@RequestBody AuthRequest request) {

        Authentication authToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword());

        return authenticationManager.authenticate(authToken)
                .flatMap(auth -> {
                    UserDetails user = (UserDetails) auth.getPrincipal();
                    AuthResponse resp = new AuthResponse(jwtUtils.generateToken(user));
                    Long orgId = Long.parseLong(user.getUsername());
                    return kafkaProducer.sendOrganizationLoggedIn(orgId).thenReturn(resp);
                });
    }
}
