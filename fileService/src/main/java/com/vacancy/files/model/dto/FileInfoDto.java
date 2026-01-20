package com.vacancy.files.model.dto;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class FileInfoDto {
    private String uuid;
    private OffsetDateTime createdAt;
    private Long ownerId;
    private String originalName;
}
