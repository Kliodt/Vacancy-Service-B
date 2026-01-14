package com.vacancy.organization.model;

import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Table("organization")
@Slf4j
public class Organization implements UserDetails {

    @Id
    private Long id;

    @NotBlank(message = "Nickname не может быть пустым")
    @Size(max = 50, message = "Nickname не может превышать 50 символов")
    private String nickname;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен иметь правильный формат")
    @Size(max = 100, message = "Email не может превышать 100 символов")
    private String email;

    @NotBlank(message = "Password не может быть пустым")
    private String password;

    public void updateWithOther(Organization other) {
        this.setNickname(other.getNickname());
        this.setEmail(other.getEmail());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ORGANIZATION"));
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
