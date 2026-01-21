package com.vacancy.files.kafka;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vacancy.files.model.FileObject;
import com.vacancy.files.repository.FileRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaConsumerService {

    private final FileRepository fileRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path storageDir;

    public KafkaConsumerService(FileRepository fileRepository, @Value("${files.storage-dir}") String storageDir) {
        this.storageDir = Paths.get(storageDir);
        this.fileRepository = fileRepository;
    }

    private Long extractLong(JsonNode node, String field) {
        if (node == null)
            throw new RuntimeException("Node is null");
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.isNull())
            throw new RuntimeException("Can't parse this field");
        try {
            return valueNode.isNumber() ? valueNode.asLong() : Long.parseLong(valueNode.asText());
        } catch (Exception e) {
            throw new RuntimeException("Can't parse this field");
        }
    }

    @KafkaListener(topics = "user.deleted", groupId = "file-service")
    public void handleUserDeleted(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            Long userId = extractLong(json, "userId");
            deleteAllFilesByUser(userId);
            log.info("Received user.deleted for userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to handle user.deleted message: {}", message, e);
        }
    }

    private void deleteAllFilesByUser(Long userId) {
        List<FileObject> files = fileRepository.findAllByOwnerId(userId);

        for (FileObject file : files) {
            try {
                fileRepository.delete(file);
                Files.deleteIfExists(storageDir.resolve(file.getUuid()));
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", file.getUuid(), e);
            }
        }
    }
}
