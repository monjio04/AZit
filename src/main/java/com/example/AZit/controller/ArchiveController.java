package com.example.AZit.controller;

import com.example.AZit.dto.response.ApiResponse;
import com.example.AZit.dto.response.ArchiveAllResponse;
import com.example.AZit.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @GetMapping("/archive")
    public ResponseEntity<ApiResponse> getArchive() {
        List<ArchiveAllResponse> response = archiveService.getAllArchive();

        return ResponseEntity.ok(
                new ApiResponse(true, 200, "아카이브 목록 반환", response)
        );
    }

}
