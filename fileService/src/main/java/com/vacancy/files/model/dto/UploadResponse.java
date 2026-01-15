package com.vacancy.files.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResponse {
    private String fileId;
    private String uploadUrl;
    private int expiresIn;
}
