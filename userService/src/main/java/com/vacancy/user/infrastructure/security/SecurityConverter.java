package com.vacancy.user.infrastructure.security;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import com.vacancy.user.domain.model.CurrentUser;
import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.model.User;

@Component
public class SecurityConverter {
    public CurrentUser extractCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            Set<Role> roles = user.getRoles() != null ? user.getRoles() : new HashSet<>();
            return new CurrentUser(user.getId(), roles);
        }

        return null;
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
