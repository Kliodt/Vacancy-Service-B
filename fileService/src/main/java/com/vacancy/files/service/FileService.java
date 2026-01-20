package com.vacancy.files.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.vacancy.files.model.FileObject;

public interface FileService {
    public FileObject getFileById(String id);
    public FileObject uploadFile(MultipartFile file);
    public void deleteFile(String uuid);
    public void deleteAllByUser(Long userId);
    public FileObject getFileWithData(String uuid);
    public List<FileObject> listAllMyFiles();
}
