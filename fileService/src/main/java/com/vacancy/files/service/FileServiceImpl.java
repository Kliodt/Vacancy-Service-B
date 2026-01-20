package com.vacancy.files.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vacancy.files.exceptions.RequestException;
import com.vacancy.files.model.FileObject;
import com.vacancy.files.repository.FileRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@PreAuthorize("isAuthenticated()")
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final Path storageDir;

    private static final List<String> ALLOWED_MIMES = List.of("image/png", "image/jpeg", "application/pdf");

    public FileServiceImpl(FileRepository fileRepository, @Value("${files.storage-dir}") String storageDir) {
        this.storageDir = Paths.get(storageDir);
        this.fileRepository = fileRepository;
    }

    public FileObject getFileById(String id) {
        Optional<FileObject> f = fileRepository.findById(id);
        if (f.isEmpty())
            throw new RequestException(HttpStatus.NOT_FOUND, "Файл не найден");
        return f.get();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public FileObject uploadFile(MultipartFile file, Authentication auth) {

        String mime = file.getContentType();

        if (!ALLOWED_MIMES.contains(mime))
            throw new RequestException(HttpStatus.BAD_REQUEST, "Данный тип файла (" + mime + ") загружать нельзя");

        FileObject fileObj = new FileObject(mime, (Long) auth.getPrincipal(), file.getOriginalFilename());

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
    public void deleteFile(String uuid, Authentication auth) {
        FileObject file = getFileById(uuid);

        if (!auth.getPrincipal().equals(file.getOwnerId()))
            throw new RequestException(HttpStatus.FORBIDDEN, "Можно удалять только свои файлы");

        try {
            fileRepository.delete(file);
            Files.deleteIfExists(storageDir.resolve(uuid));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", uuid, e);
        }
    }

    public void deleteAllByUser(Long userId) {
        List<FileObject> files = fileRepository.findAllByOwnerId(userId);

        for (FileObject file : files) {
            try {
                fileRepository.delete(file);
                Files.deleteIfExists(storageDir.resolve(file.getUuid()));
            } catch (IOException e) {
                log.warn("Failed to delete file: {}", file.getUuid(), e);
            }
        }
    }

    public FileObject getFileWithData(String uuid) {
        Path p = storageDir.resolve(uuid);
        FileObject file = getFileById(uuid);
        if (!Files.exists(p))
            throw new RequestException(HttpStatus.NOT_FOUND, "Файл не найден");
        file.setResource(new FileSystemResource(p));
        return file;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public List<FileObject> listAllMyFiles(Authentication auth) {
        return fileRepository.findAllByOwnerId((Long) auth.getPrincipal());
    }
}
