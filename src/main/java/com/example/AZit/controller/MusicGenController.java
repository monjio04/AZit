package com.example.AZit.controller;

import com.example.AZit.dto.response.ApiResponse;
import com.example.AZit.dto.response.MusicGenResponse;
import com.example.AZit.service.MusicGenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/replicate/musicgen")
@RequiredArgsConstructor
public class MusicGenController {

    private final MusicGenService musicGenService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MusicGenResponse>> createSong(@RequestBody Map<String, Long> request) {
        Long elementId = request.get("elementId");

        if (elementId == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(true,400, "❌ elementId가 필요합니다.", null)
            );
        }

        try {
            MusicGenResponse response = musicGenService.createSong(elementId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true,200, "✅ 음악 생성 성공", response)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new ApiResponse<>(false,500, "❌ 음악 생성 실패: " + e.getMessage(), null));
        }
    }
}
