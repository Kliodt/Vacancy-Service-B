package com.vacancy.vacancy.client;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Clients {

    private final OrganizationClient organizationClient;
    private final UserClient userClient;

    @CircuitBreaker(name = "organization-service")
    public Object getOrganizationById(long orgId) {
        String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        return organizationClient.getOrganizationById(orgId, "Bearer " + token);
    }

    @CircuitBreaker(name = "user-service")
    public Object getUserById(long userId) {
        String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        return userClient.getUserById(userId, "Bearer " + token);
    }
}
