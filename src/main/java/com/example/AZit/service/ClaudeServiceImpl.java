package com.example.AZit.service;

import com.example.AZit.config.ClaudeConfig;
import com.example.AZit.dto.ClaudeRequestApiDto;
import com.example.AZit.dto.ClaudeResponseApiDto;
import com.example.AZit.service.ClaudeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaudeServiceImpl implements ClaudeService {
    private final ClaudeConfig claudeConfig;

    private String modelVersion = "claude-3-haiku-20240307";	//사용할 모델명
    private int maxTokens = 1000;					//최대 사용 가능한 토큰 수

    /*
     * 입력 매개변수 : "질문내용"
     * 출력 : api의 답변
     * */
    public Mono<ClaudeResponseApiDto> sendApiRequest(String prompt) {
        ClaudeRequestApiDto request = ClaudeRequestApiDto.builder()		//요청 DTO 생성하기
                .model(modelVersion)
                .messages(List.of(ClaudeRequestApiDto.Message.builder()
                        .role("user")
                        .content(prompt)
                        .build()))
                .max_tokens(maxTokens)
                .build();

        return claudeConfig.getWebClient().post()		//POST 요청하고 답변받기
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClaudeResponseApiDto.class)
                .onErrorResume(error -> {				//에러날 시 error 답변 받기
                    System.out.println("Claude Api Error: "+ error.getMessage());
                    return Mono.just(ClaudeResponseApiDto.getClaudeErrorDto(error.getMessage()));
                });
    }


}