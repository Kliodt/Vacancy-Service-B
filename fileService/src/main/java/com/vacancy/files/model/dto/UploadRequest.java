package com.vacancy.files.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UploadRequest {
    @NotBlank
    private String filename;

    @NotBlank
    private String mime;
}
