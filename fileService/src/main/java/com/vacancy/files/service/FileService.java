package com.vacancy.files.service;

import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vacancy.files.dto.PresignedUrlResponse;
import com.vacancy.files.repository.FileRepository;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
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

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public void afterPropertiesSet() {
        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(minioEndpoint))
                .region(Region.of("us-east-1"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(minioAccessKey, minioSecretKey)))
                .build();
    }

    public PresignedUrlResponse generateUploadUrl(String key, String contentType) {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(minioBucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignExpirySeconds))
                .putObjectRequest(putReq)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignReq);
        return new PresignedUrlResponse(presigned.url().toString(), presigned.httpRequest().method().name());
    }

    public PresignedUrlResponse generateDownloadUrl(String key) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(minioBucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignExpirySeconds))
                .getObjectRequest(getReq)
                .build();

        PresignedGetObjectRequest presigned = presigner.presignGetObject(presignReq);
        return new PresignedUrlResponse(presigned.url().toString(), presigned.httpRequest().method().name());
    }
}
