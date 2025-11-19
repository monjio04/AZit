package com.example.AZit.controller;

import com.example.AZit.dto.response.ApiResponse;
import com.example.AZit.dto.response.ArchiveAllResponse;
import com.example.AZit.dto.response.ArchiveDetailResponse;
import com.example.AZit.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    // [추가] 프론트엔드가 "이 노래 다운로드 링크 줘!" 라고 요청하는 곳
    @GetMapping("/download-url")
    public ResponseEntity<String> getDownloadUrl(@RequestParam Long songId) {

        // 방금 만든 서비스 메소드 호출
        String url = archiveService.generatePresignedUrl(songId);

        // 생성된 긴~ AWS URL을 그대로 프론트엔드에게 전달
        return ResponseEntity.ok(url);
    }

}
