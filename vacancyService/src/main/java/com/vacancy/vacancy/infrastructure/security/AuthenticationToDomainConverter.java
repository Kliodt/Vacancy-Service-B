package com.vacancy.vacancy.infrastructure.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.BadCredentialsException;
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
            throw new BadCredentialsException("Can't convert unauthenticated user");
        }
        
        Long userId = (Long) auth.getPrincipal();
        Set<Role> roles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(this::parseRole)
            .collect(Collectors.toSet());
        
        return new CurrentUser(userId, roles);
    }
    
    /**
     * Преобразует строку роли в enum
     */
    private Role parseRole(String authority) {
        return switch (authority) {
            case "ROLE_ORGANIZATION" -> Role.ORGANIZATION;
            case "ROLE_USER" -> Role.USER;
            default -> Role.USER;
        };
    }
}
