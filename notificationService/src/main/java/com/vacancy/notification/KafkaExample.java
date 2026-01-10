package com.vacancy.notification;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class KafkaExample {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/send/{topic}")
    public String sendMessage(@PathVariable String topic, @RequestParam String message) {
        kafkaTemplate.send(topic, message);
        return "Message sent to topic " + topic;
    }

    @KafkaListener(topics = {"A","B","C","D","E"}, groupId = "demo-group")
    public void listen(ConsumerRecord<String, String> consumerRecord) {
        log.info("Received message from topic " + consumerRecord.topic() + ": " + consumerRecord.value());
    }

}
