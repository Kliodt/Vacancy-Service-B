package com.vacancy.organization.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.vacancy.organization.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final OrganizationRepository organizationRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        // Organization implements UserDetails, username here is organization email
        return organizationRepository.findOrganizationByEmail(username).map(x -> x);
    }

}
