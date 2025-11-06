package com.example.AZit.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class NcpStorageService {

    private final S3Client s3Client;

    @Value("${cloud.ncp.storage.bucket}")
    private String bucket;

    // Path로 업로드 가능하도록 오버로드
    public String uploadFile(String key, Path filePath) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(filePath.toFile()));

        return String.format("https://kr.object.ncloudstorage.com/%s/%s", bucket, key);
    }
}

