package com.vacancy.user.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Nickname не может быть пустым")
    @Size(max = 50, message = "Nickname не может превышать 50 символов")
    private @NotNull String nickname;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен иметь правильный формат")
    @Size(max = 100, message = "Email не может превышать 100 символов")
    private @NotNull String email;

    @Column(nullable = false)
    @NotBlank(message = "Password не может быть пустым")
    private @NotNull String password;

    @Column(nullable = true, length = 512)
    @Size(max = 512, message = "CV Link не может превышать 512 символов")
    private @Nullable String cvLink;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_favorites", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "vacancy_id")
    private List<Long> favoriteVacancyIds = new ArrayList<>(); // ID избранных вакансий

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    public void updateWithOther(User other) {
        this.setNickname(other.getNickname());
        this.setEmail(other.getEmail());
        this.setCvLink(other.getCvLink());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.name())).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return String.valueOf(this.id);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
