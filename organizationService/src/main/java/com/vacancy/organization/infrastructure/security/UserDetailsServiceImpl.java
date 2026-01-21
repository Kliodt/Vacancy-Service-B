package com.vacancy.organization.infrastructure.security;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.vacancy.organization.infrastructure.persistance.repository.OrganizationR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final OrganizationR2dbcRepository organizationRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        // Organization implements UserDetails, username here is organization email
        return organizationRepository.findByEmail(username).map(CustomUserDetails::new);
    }

}
