package com.example.AZit.controller;

import com.example.AZit.dto.response.ApiResponse;
import com.example.AZit.dto.response.ArchiveAllResponse;
import com.example.AZit.dto.response.ArchiveDetailResponse;
import com.example.AZit.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/archive")
public class ArchiveController {

    private final ArchiveService archiveService;

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
