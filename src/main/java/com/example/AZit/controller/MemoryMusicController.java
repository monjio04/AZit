package com.example.AZit.controller;

import com.example.AZit.dto.request.MemoryRequest;
import com.example.AZit.dto.response.ApiResponse;
import com.example.AZit.dto.response.MemoryCreateResponse;
import com.example.AZit.dto.response.MusicElementsResponse;
import com.example.AZit.service.MemoryMusicService;
import com.example.AZit.service.MemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users/{userId}/memory")
@RequiredArgsConstructor
public class MemoryMusicController {

    private final MemoryService memoryService;
    private final MemoryMusicService memoryMusicService;

    @PostMapping
    public ResponseEntity<ApiResponse> createMemory(@PathVariable Long userId, @RequestBody MemoryRequest request) {
        MemoryCreateResponse response=memoryService.createMemory(userId,request);

        return ResponseEntity.ok(new ApiResponse(true,201,"memory 등록 완료",response));
    }

    @PostMapping("/{memoryId}/analyze")
    public Mono<ResponseEntity<MusicElementsResponse>> analyzeMemory(@PathVariable Long memoryId) {
        return memoryMusicService.analyzeMemory(memoryId)
                .map(ResponseEntity::ok);
    }


}
