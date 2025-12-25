package com.vacancy.organization.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.SecurityWebFilterChain;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, exAuth) -> {
                            // avoid adding WWW-Authenticate
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, denied) -> {
                            // avoid adding WWW-Authenticate
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }))
                .authorizeExchange(ex -> ex
                        .pathMatchers(
                                "/auth/**",
                                "/organization/v3/api-docs/**",
                                "/organization/swagger-ui/**")
                        .permitAll()
                        .anyExchange().authenticated())

                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .build();
    }

    @Bean
    public ReactiveUserDetailsService nopReactiveUserDetailsService() {
        return str -> Mono.error(new RuntimeException("Authentication is disabled. Use JWT :)"));
    }


}