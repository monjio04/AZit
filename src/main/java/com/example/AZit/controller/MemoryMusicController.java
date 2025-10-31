package com.example.AZit.controller;

import com.example.AZit.dto.ClaudeResponseApiDto;
import com.example.AZit.dto.MemoryRequestDto;
import com.example.AZit.dto.MusicElementsResponseDto;
import com.example.AZit.service.ClaudeServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MemoryMusicController {

    private final ClaudeServiceImpl claudeService;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/analyze")
    public Mono<MusicElementsResponseDto> analyzeMemory(@RequestBody MemoryRequestDto request) {
        // 🎵 프롬프트 구성
        String prompt = """
            아래는 사용자가 회상한 추억과 감정입니다.

            [언제]
            %s

            [추억 내용]
            %s

            [감정]
            %s

            위 내용을 바탕으로 다음 항목을 JSON 형식으로 작성해주세요:
            {
              "keywords": ["..."],
              "mood": "happy | sad | nostalgic | peaceful ...",
              "scale": "major | minor",
              "tempo": "slow | moderate | fast",
              "atmosphere": "bright | dark | dreamy | energetic ..."
            }

            JSON만 반환해주세요.
            """.formatted(request.getWhen(),request.getMemory(), request.getEmotion());

        return claudeService.sendApiRequest(prompt)
                .map(response -> {
                    try {
                        String content = response.getTextContent().trim();
                        return mapper.readValue(content, MusicElementsResponseDto.class);
                    } catch (Exception e) {
                        System.out.println("Parsing Error: " + e.getMessage());
                        return new MusicElementsResponseDto(null, "unknown", "major", "moderate", "neutral");
                    }
                });
    }
}
