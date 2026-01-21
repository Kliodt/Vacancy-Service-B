package com.vacancy.user.infrastructure.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;

@Component
public class SecurityConverter {
    public CurrentUser extractCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return extractCurrentUserFromAuthorities((Long) authentication.getPrincipal(), authentication);
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
