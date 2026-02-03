package com.vacancy.files.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import com.vacancy.files.model.FileObject;

public interface FileService {
    public FileObject getFileById(String id);
    public FileObject uploadFile(MultipartFile file, Authentication auth);
    public void deleteFile(String uuid, Authentication auth);
    public FileObject getFileWithData(String uuid);
    public List<FileObject> listAllMyFiles(Authentication auth);
}
