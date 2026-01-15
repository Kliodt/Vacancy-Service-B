package com.vacancy.organization.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Void> sendOrganizationDeleted(Long organizationId) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("organizationId", organizationId);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("organization.deleted", node.toString());
        SenderRecord<String, String, Void> senderRecord = SenderRecord.create(producerRecord, null);
        return kafkaSender.send(Mono.just(senderRecord)).then()
                .doOnError(e -> log.error("Failed to send organization.deleted event for id={}", organizationId, e));
    }

    public Mono<Void> sendOrganizationLoggedIn(String email) {
    public Mono<Void> sendOrganizationLoggedIn(Long organizationId) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("organizationId", organizationId);
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("organization.login", node.toString());
        SenderRecord<String, String, Void> senderRecord = SenderRecord.create(producerRecord, null);
        return kafkaSender.send(Mono.just(senderRecord)).then()
                .doOnError(e -> log.error("Failed to send organization.login event for id={}", organizationId, e));
    }

}
