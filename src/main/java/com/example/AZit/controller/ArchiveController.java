package com.example.AZit.controller;

import com.example.AZit.dto.response.ApiResponse;
import com.example.AZit.dto.response.ArchiveAllResponse;
import com.example.AZit.dto.response.ArchiveDetailResponse;
import com.example.AZit.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/archive")
public class ArchiveController {

    private final S3Presigner s3Presigner;
    private final ArchiveService archiveService;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucket;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllArchive() {
        List<ArchiveAllResponse> response = archiveService.getAllArchive();

        return ResponseEntity.ok(
                new ApiResponse(true, 200, "아카이브 목록 반환", response)
        );
    }

    @GetMapping("/{songId}")
    public ResponseEntity<ApiResponse> getDetailArchive(
            @PathVariable Long songId
    ){
        ArchiveDetailResponse response=archiveService.getDetailArchive(songId);

        return ResponseEntity.ok(new ApiResponse<>(true,200,"아카이브 반환 성공",response));
    }

}
