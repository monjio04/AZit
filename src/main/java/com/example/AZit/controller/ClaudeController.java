package com.example.AZit.controller;

import com.example.AZit.dto.ClaudeResponseApiDto;
import com.example.AZit.service.ClaudeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/claude")
public class ClaudeController {
    private final ClaudeService claudeService;

    @PostMapping("/")
    public Mono<ClaudeResponseApiDto> apiRequest(@RequestBody String prompt) {
        return claudeService.sendApiRequest(prompt);
    }
}