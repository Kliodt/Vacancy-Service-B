package com.vacancy.user.infrastructure.messaging;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vacancy.user.domain.port.UserEventPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUserEventAdapter implements UserEventPort {
    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> publishUserDeleted(Long userId) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("userId", userId);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("user.deleted", node.toString());
        SenderRecord<String, String, Void> senderRecord = SenderRecord.create(producerRecord, null);
        return kafkaSender.send(Mono.just(senderRecord)).then()
                .doOnError(e -> log.error("Failed to send user.deleted event for id={}", userId, e));
    }

    @Override
    public Mono<Void> publishUserLoggedIn(Long userId) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("userId", userId);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("user.login", node.toString());
        SenderRecord<String, String, Void> senderRecord = SenderRecord.create(producerRecord, null);
        return kafkaSender.send(Mono.just(senderRecord)).then()
            .doOnError(e -> log.error("Failed to send user.login event for id={}", userId, e));
    }
}
