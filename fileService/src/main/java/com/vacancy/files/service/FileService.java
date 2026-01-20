package com.vacancy.files.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vacancy.files.exceptions.RequestException;
import com.vacancy.files.model.FileObject;
import com.vacancy.files.repository.FileRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@PreAuthorize("isAuthenticated()")
@Slf4j
public class FileService {

    private final FileRepository fileRepository;
    private final Path storageDir;

    public FileService(FileRepository fileRepository, @Value("${files.storage-dir}") String storageDir) {
        this.storageDir = Paths.get(storageDir);
        this.fileRepository = fileRepository;
    }

    private Long getCurrentPrincipalId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public FileObject getFileById(String id) {
        FileObject f = fileRepository.findById(id).orElse(null);
        if (f == null)
            throw new RequestException(HttpStatus.NOT_FOUND, "Файл не найден");
        return f;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public FileObject uploadFile(MultipartFile file) {
        Long principalId = getCurrentPrincipalId();

        String mime = file.getContentType();
        FileObject fileObj = new FileObject(mime, principalId, file.getOriginalFilename());

        try {
            Path target = storageDir.resolve(fileObj.getUuid());
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return fileRepository.save(fileObj);
        } catch (IOException e) {
            throw new RequestException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось сохранить файл");
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public void deleteFile(String uuid) {
        FileObject file = getFileById(uuid);
        Long principalId = getCurrentPrincipalId();

        if (!principalId.equals(file.getOwnerId()))
            throw new RequestException(HttpStatus.FORBIDDEN, "Можно удалять только свои файлы");

        try {
            fileRepository.delete(file);
            Files.deleteIfExists(storageDir.resolve(uuid));
        } catch (IOException e) {
            log.warn("Failed to delete file from disk: {}", uuid, e);
        }
    }

    public FileObject getFileWithData(String uuid) {
        Path p = storageDir.resolve(uuid);
        if (!Files.exists(p))
            return null;
        FileObject file = getFileById(uuid);
        file.setResource(new FileSystemResource(p));
        return file;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public List<FileObject> listAllMyFiles() {
        Long principalId = getCurrentPrincipalId();
        return fileRepository.findAllByOwnerId(principalId);
    }

}
