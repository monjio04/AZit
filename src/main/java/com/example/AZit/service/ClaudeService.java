package com.example.AZit.service;

import com.example.AZit.dto.ClaudeResponseApiDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface ClaudeService {
    Mono<ClaudeResponseApiDto> sendApiRequest(String prompt);
}
