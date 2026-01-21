package com.vacancy.user.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import com.vacancy.user.domain.port.UserEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUserEventAdapter implements UserEventPort {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String USER_DELETED_TOPIC = "user-deleted";
    private static final String USER_LOGGED_IN_TOPIC = "user-logged-in";

    @Override
    public Mono<Void> publishUserDeleted(Long userId) {
        return Mono.fromRunnable(() -> {
            try {
                kafkaTemplate.send(USER_DELETED_TOPIC, userId.toString());
                log.info("Published user deleted event: userId = {}", userId);
            } catch (Exception e) {
                log.error("Error publishing user deleted event: userId = {}", userId, e);
            }
        })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    @Override
    public Mono<Void> publishUserLoggedIn(Long userId) {
        return Mono.fromRunnable(() -> {
            try {
                kafkaTemplate.send(USER_LOGGED_IN_TOPIC, userId.toString());
                log.info("Published user logged in event: userId = {}", userId);
            } catch (Exception e) {
                log.error("Error publishing user logged in event: userId = {}", userId, e);
            }
        })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }
}
