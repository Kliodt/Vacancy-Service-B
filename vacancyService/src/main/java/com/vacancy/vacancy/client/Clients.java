package com.vacancy.vacancy.client;

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
        return organizationClient.getOrganizationById(orgId);
    }

    @CircuitBreaker(name = "user-service")
    public Object getUserById(long userId) {
        return userClient.getUserById(userId);
    }
}
