package com.vacancy.files.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class FileObject {

    public enum Status {
        REQUESTED, SAVED, DELETED
    }

    @Column(nullable = false, unique = true)
    @Id
    private String uuid;

    @Column(nullable = false)
    private String mime;

    @Enumerated(EnumType.STRING)
    private Status status;

    private OffsetDateTime createdAt;

    private Long ownerId;

    public FileObject(String mime, Status status, Long ownerId) {
        this.mime = mime;
        this.status = status;
        this.ownerId = ownerId;
        this.createdAt = OffsetDateTime.now();
        this.uuid = UUID.randomUUID().toString();
    }

}
