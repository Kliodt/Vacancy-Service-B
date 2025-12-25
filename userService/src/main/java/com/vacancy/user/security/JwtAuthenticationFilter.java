package com.vacancy.user.security;

import java.util.Objects;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Authorization"))
                .filter(h -> h.startsWith("Bearer "))
                .map(h -> h.substring(7))
                .map(jwtUtils::tokenToAuthentication)
                .filter(Objects::nonNull)
                .flatMap(auth -> chain
                        .filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                .switchIfEmpty(chain.filter(exchange));
    }

}
