package com.example.AZit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class NcpObjectStorageConfig {

    @Value("${cloud.ncp.storage.access-key}")
    private String accessKey;

    @Value("${cloud.ncp.storage.secret-key}")
    private String secretKey;

    @Value("${cloud.ncp.storage.region}")
    private String region;

    @Value("${cloud.ncp.storage.end-point}")
    private String endPoint;

    @Bean
    public S3Client ncpS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endPoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .region(Region.of(region))
                .build();
    }
}
