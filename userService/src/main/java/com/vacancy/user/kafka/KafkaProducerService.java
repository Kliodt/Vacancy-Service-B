package com.vacancy.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaSender<String, String> kafkaSender;

    public Mono<Void> sendUserDeleted(Long userId) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("user.deleted", String.valueOf(userId));
        SenderRecord<String, String, Void> senderRecord = SenderRecord.create(producerRecord, null);
        return kafkaSender.send(Mono.just(senderRecord)).then()
                .doOnError(e -> log.error("Failed to send user.deleted event for id={}", userId, e));
    }

    public Mono<Void> sendUserLoggedIn(String email) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("user.login", email);
        SenderRecord<String, String, Void> senderRecord = SenderRecord.create(producerRecord, null);
        return kafkaSender.send(Mono.just(senderRecord)).then()
                .doOnError(e -> log.error("Failed to send user.login event for email={}", email, e));
    }

}
