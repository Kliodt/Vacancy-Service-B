package com.vacancy.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileDataRepository fileRepository;

    @Value("${filestorage.path}")
    private String storagePath;

    @NonNull
    @SuppressWarnings("null")
    private Path pathByFileId(long id) {
        return Path.of(storagePath + Long.toString(id));
    }

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
            return null;
        }
        return fileData;
    }

    public FileData loadFromFileSystem(long id) throws IOException {
        FileData fileData = fileRepository.findById(id).orElse(null);
        if (fileData == null) {
            throw new IOException("File data wasn't found in database");
        }
        byte[] data = Files.readAllBytes(pathByFileId(fileData.getId()));
        fileData.setData(data);
        return fileData;
    }

    public void deleteFile(long id) throws IOException {
        FileData fileData = fileRepository.findById(id).orElse(null);
        if (fileData == null) return;

        Files.deleteIfExists(pathByFileId(fileData.getId()));
        fileRepository.delete(fileData);
    }
}