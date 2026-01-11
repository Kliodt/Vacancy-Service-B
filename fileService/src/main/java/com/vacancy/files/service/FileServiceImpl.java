package com.vacancy.files.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vacancy.files.exceptions.RequestException;
import com.vacancy.files.model.FileData;
import com.vacancy.files.repository.FileDataRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileDataRepository fileRepository;

    @Value("${filestorage.path}")
    private String storagePath;

    @NonNull
    @SuppressWarnings("null")
    private Path pathByFileId(long id) {
        return Path.of(storagePath + Long.toString(id));
    }

    @Override
    public FileData saveToFileSystem(MultipartFile file) {
        String origName = file.getOriginalFilename();
        if (origName == null || origName.isEmpty()) {
            origName = "file";
        }

        FileData fileData = new FileData(origName);
        fileData = fileRepository.save(fileData);

        try {
            file.transferTo(pathByFileId(fileData.getId()));
        } catch (IOException e) {
            fileRepository.delete(fileData);
            throw new RequestException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
        return fileData;
    }

    @Override
    public FileData loadFromFileSystem(long id) {
        FileData fileData = fileRepository.findById(id).orElse(null);
        if (fileData == null) {
            throw new RequestException(HttpStatus.NOT_FOUND, "File data wasn't found in database");
        }
        try {
            byte[] data = Files.readAllBytes(pathByFileId(fileData.getId()));
            fileData.setData(data);
            return fileData;
        } catch (IOException e) {
            throw new RequestException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file from storage");
        }
    }

    @Override
    public void deleteFile(long id) {
        FileData fileData = fileRepository.findById(id).orElse(null);
        if (fileData == null) return;

        try {
            Files.deleteIfExists(pathByFileId(fileData.getId()));
            fileRepository.delete(fileData);
        } catch (IOException e) {
            throw new RequestException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file");
        }
    }
}
