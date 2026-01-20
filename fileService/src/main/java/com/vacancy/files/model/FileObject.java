package com.vacancy.files.model;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class FileObject {

    @Column(nullable = false, unique = true)
    @Id
    private String uuid;

    @Column(nullable = false)
    private String mime;

    private OffsetDateTime createdAt;

    private Long ownerId;
    
    private String originalName;

    @Transient
    Resource resource;

    public FileObject(String mime, Long ownerId, String filename) {
        this.mime = mime;
        this.ownerId = ownerId;
        this.createdAt = OffsetDateTime.now();
        this.uuid = UUID.randomUUID().toString();
        this.originalName = filename;
    }

}
