package com.vacancy.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Void> sendUserDeleted(Long userId) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("userId", userId);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("user.deleted", node.toString());
        SenderRecord<String, String, Void> senderRecord = SenderRecord.create(producerRecord, null);
        return kafkaSender.send(Mono.just(senderRecord)).then()
                .doOnError(e -> log.error("Failed to send user.deleted event for id={}", userId, e));
    }

    public Mono<Void> sendUserLoggedIn(Long userId) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("userId", userId);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("user.login", node.toString());
        SenderRecord<String, String, Void> senderRecord = SenderRecord.create(producerRecord, null);
        return kafkaSender.send(Mono.just(senderRecord)).then()
            .doOnError(e -> log.error("Failed to send user.login event for id={}", userId, e));
    }

}
