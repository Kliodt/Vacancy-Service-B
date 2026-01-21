package com.vacancy.user.infrastructure.persistence.repository;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vacancy.user.domain.model.Role;
import com.vacancy.user.domain.model.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SuperUserInitializer {

    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${supervisor.email}")
    private String suEmail;

    @Value("${supervisor.pass}")
    private String suPass;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (suEmail == null || suEmail.isEmpty())
            return;
        User su = userRepository.findByEmail(suEmail)
                .orElse(new User("Root", suEmail, passwordEncoder.encode(suPass)));
        su.getRoles().add(Role.SUPERVISOR);
        su.getRoles().add(Role.USER);
        userRepository.save(su);
    }
}