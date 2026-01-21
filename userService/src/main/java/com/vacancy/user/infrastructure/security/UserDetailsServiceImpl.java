package com.vacancy.user.infrastructure.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.vacancy.user.infrastructure.persistence.repository.JpaUserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final JpaUserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.fromSupplier(() -> {
            UserDetails user = userRepository.findByEmail(username).map(CustomUserDetails::new).orElse(null);
            if (user == null)
                throw new BadCredentialsException("Invalid credentials");
            return user;
        })
                .subscribeOn(Schedulers.boundedElastic());
    }
}