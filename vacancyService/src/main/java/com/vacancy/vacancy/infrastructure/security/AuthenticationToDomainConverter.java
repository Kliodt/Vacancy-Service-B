package com.vacancy.vacancy.infrastructure.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.vacancy.vacancy.domain.model.CurrentUser;
import com.vacancy.vacancy.domain.model.Role;

import lombok.RequiredArgsConstructor;

/**
 * Преобразователь Spring Security Authentication в domain model CurrentUser
 * Отвязывает use cases от Spring Security
 */
@Component
@RequiredArgsConstructor
public class AuthenticationToDomainConverter {
    
    /**
     * Преобразует Authentication в CurrentUser
     */
    public CurrentUser convert(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
                
        return extractCurrentUserFromAuthorities((Long) auth.getPrincipal(), auth);
    }
    
    public CurrentUser extractCurrentUserFromAuthorities(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Set<Role> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> Role.valueOf(authority.replace("ROLE_", "")))
                .collect(Collectors.toSet());

        return new CurrentUser(userId, roles);
    }
}
