package com.example.AZit.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class StorageUploader {

    private final S3Client s3Client;

    private final String bucket = "azit";

    public String upload(Path filePath, String keyName) throws Exception {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(keyName)
                        .contentType(Files.probeContentType(filePath))
                        .build(),
                RequestBody.fromFile(filePath)
        );

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                s3Client.serviceClientConfiguration().region().id(),
                keyName);
    }
}
