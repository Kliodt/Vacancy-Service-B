package com.vacancy.files.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vacancy.files.model.FileData;

@Repository
public interface FileDataRepository extends JpaRepository<FileData,Long> {
}
