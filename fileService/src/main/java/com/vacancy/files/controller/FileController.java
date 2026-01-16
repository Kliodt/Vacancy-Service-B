package com.vacancy.files.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vacancy.files.model.FileObject;
import com.vacancy.files.model.dto.PresignedUrlResponse;
import com.vacancy.files.model.dto.UploadRequest;
import com.vacancy.files.model.dto.UploadResponse;
import com.vacancy.files.service.FileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public UploadResponse requestUpload(@Valid @RequestBody UploadRequest request) {
        return fileService.requestUpload(request);
    }

    @GetMapping("/download")
    public PresignedUrlResponse getDownloadUrl(@RequestParam String uuid) {
        return fileService.generateDownloadUrl(uuid);
    }

    @GetMapping("/{uuid}")
    public FileObject getFileInfo(@PathVariable String uuid) {
        return fileService.getFileById(uuid);
    }

    @GetMapping("/list")
    public List<FileObject> listUserFiles() {
        return fileService.getAllMyFiles();
    }

    @DeleteMapping("/{uuid}")
    public void deleteFile(@PathVariable String uuid) {
        fileService.deleteFile(uuid);
    }
}
