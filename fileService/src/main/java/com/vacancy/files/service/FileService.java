package com.vacancy.files.service;

import org.springframework.web.multipart.MultipartFile;

import com.vacancy.files.model.FileData;

public interface FileService {
    FileData saveToFileSystem(MultipartFile file);
    FileData loadFromFileSystem(long id);
    void deleteFile(long id);
}
