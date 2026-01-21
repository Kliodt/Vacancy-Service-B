package com.vacancy.organization.infrastructure.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.vacancy.organization.domain.model.CurrentUser;
import com.vacancy.organization.domain.model.Role;

/**
 * Преобразователь Spring Security Authentication в domain model CurrentUser
 * Отвязывает use cases от Spring Security
 */
@Component
public class AuthenticationToDomainConverter {
    
    public CurrentUser convert(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        
        Long organizationId = (Long) auth.getPrincipal();
        return extractCurrentUserFromAuthorities(organizationId, auth);
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
