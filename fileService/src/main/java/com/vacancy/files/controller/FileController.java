package com.vacancy.files.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vacancy.files.model.FileObject;
import com.vacancy.files.model.dto.FileInfoDto;
import com.vacancy.files.service.FileService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Operation(summary = "Загрузить файл")
    @PostMapping(value = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<FileInfoDto> requestUpload(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        FileObject fo = fileService.uploadFile(file, auth);
        return ResponseEntity.ok(modelMapper.map(fo, FileInfoDto.class));
    }

    @Operation(summary = "Скачать файл")
    @GetMapping(value = "/download/{uuid}")
    public ResponseEntity<Resource> getDownloadUrl(@PathVariable String uuid) {
        FileObject fileObject = fileService.getFileWithData(uuid);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileObject.getOriginalName() + "\"")
                .contentType(new MediaType("application", "octet-stream"))
                .body(fileObject.getResource());
    }

    @Operation(summary = "Удалить файл")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteFile(@PathVariable String uuid, Authentication auth) {
        fileService.deleteFile(uuid, auth);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить информацию о файле")
    @GetMapping("/{uuid}")
    public ResponseEntity<FileInfoDto> getFileInfo(@PathVariable String uuid) {
        FileObject fo = fileService.getFileById(uuid);
        return ResponseEntity.ok(modelMapper.map(fo, FileInfoDto.class));
    }

    @Operation(summary = "Получить все файлы текущего пользователя")
    @GetMapping("/list")
    public ResponseEntity<List<FileInfoDto>> listUserFiles(Authentication auth) {
        List<FileInfoDto> resp = fileService.listAllMyFiles(auth).stream()
                .map(fo -> modelMapper.map(fo, FileInfoDto.class))
                .toList();
        return ResponseEntity.ok(resp);
    }
}
