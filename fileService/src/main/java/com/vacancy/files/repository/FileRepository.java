package com.vacancy.files.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vacancy.files.model.FileObject;

@Repository
public interface FileRepository extends JpaRepository<FileObject,Long> {
    // List<FileObject> getAllFileObjectsByKeyAndOwnerId(String key, long ownerId);
}
