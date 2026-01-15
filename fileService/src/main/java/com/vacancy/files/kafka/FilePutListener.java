package com.vacancy.files.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacancy.files.model.FileObject;
import com.vacancy.files.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilePutListener {

    private final FileRepository fileRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "file.put", groupId = "file-service-group")
    public void onMessage(String payload) {
        log.info("Received file.put message: {}", payload);
        try {
            JsonNode root = objectMapper.readTree(payload);

            JsonNode s3 = root.at("/Records/0/s3");
            String key = s3.at("/object/key").asText();

            // Key is uuid in our system
            FileObject file = fileRepository.findById(key).orElse(null);
            if (file == null) {
                log.warn("file.put: file with uuid {} not found", key);
                return;
            }

            file.setStatus(FileObject.Status.SAVED);
            fileRepository.save(file);
            log.info("file.put: file {} status set to SAVED", key);

        } catch (Exception e) {
            log.error("Error processing file.put message", e);
        }
    }
}
