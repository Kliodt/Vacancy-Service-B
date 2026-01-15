package com.vacancy.files.service;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.vacancy.files.exceptions.RequestException;
import com.vacancy.files.model.FileObject;
import com.vacancy.files.model.dto.PresignedUrlResponse;
import com.vacancy.files.model.dto.UploadRequest;
import com.vacancy.files.model.dto.UploadResponse;
import com.vacancy.files.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FileService implements InitializingBean {

    private final FileRepository fileRepository;
    private S3Presigner presigner;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.access-key}")
    private String minioAccessKey;

    @Value("${minio.secret-key}")
    private String minioSecretKey;

    @Value("${minio.bucket-name}")
    private String minioBucket;

    @Value("${minio.presign-expiry-seconds:3600}")
    private int presignExpirySeconds;

    private static final List<String> ALLOWED_MIMES = List.of("image/jpeg", "image/png", "application/pdf");

    @Override
    public void afterPropertiesSet() {
        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(minioEndpoint))
                .region(Region.of("us-east-1"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(minioAccessKey, minioSecretKey)))
                .build();
    }

    public FileObject getFileById(String id) {
        FileObject f = fileRepository.findById(id).orElse(null);
        if (f == null)
            throw new RequestException(HttpStatus.NOT_FOUND, "Файл не найден");
        if (f.getStatus() != FileObject.Status.SAVED)
            throw new RequestException(HttpStatus.NOT_FOUND, "Файл еще не загружен");
        return f;
    }

    public PresignedUrlResponse generateDownloadUrl(String uuid) {
        getFileById(uuid); // check that exists

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignExpirySeconds))
                .getObjectRequest(builder -> builder
                        .bucket(minioBucket)
                        .key("files/" + uuid) // bucket_name + uuid
                )
                .build();

        PresignedGetObjectRequest presigned = presigner.presignGetObject(presignReq);

        return new PresignedUrlResponse(presigned.url().toString(), presigned.httpRequest().method().name());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    public UploadResponse requestUpload(UploadRequest request) {

        Long principalId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // basic checks
        if (!ALLOWED_MIMES.contains(request.getMime()))
            throw new RequestException(HttpStatus.BAD_REQUEST, "Этот тип файлов запрещен");

        // create DB record with status UPLOADING
        FileObject file = fileRepository
                .save(new FileObject(request.getMime(), FileObject.Status.REQUESTED, principalId));

        // generate presigned upload url
        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignExpirySeconds))
                .putObjectRequest(
                        builder -> builder
                                .bucket(minioBucket)
                                .key("files/" + file.getUuid()) // bucket_name + file_name
                                .contentType(request.getMime()))
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignReq);

        return new UploadResponse(file.getUuid(), presigned.url().toString(), presignExpirySeconds);
    }

}
