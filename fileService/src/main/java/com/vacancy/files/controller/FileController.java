package com.vacancy.files.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.files.dto.PresignedUrlResponse;
import com.vacancy.files.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public PresignedUrlResponse getUploadUrl(@RequestParam String key,
            @RequestParam(required = false, defaultValue = "application/octet-stream") String contentType) {
        return fileService.generateUploadUrl(key, contentType);
    }

    @GetMapping("/download")
    public PresignedUrlResponse getDownloadUrl(@RequestParam String key) {
        return fileService.generateDownloadUrl(key);
    }
}
