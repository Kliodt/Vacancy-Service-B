package com.vacancy.user.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.vacancy.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.fromSupplier(() -> userRepository.findUserByEmail(username))
                .<UserDetails>flatMap(userOpt -> userOpt
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new BadCredentialsException("Invalid credentials"))))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
