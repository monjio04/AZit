package com.example.AZit.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClaudeConfig {
    @Getter
    private WebClient webClient;

    public ClaudeConfig(@Value("${claude-api.api-key}") String secretKey,
                        @Value("${claude-api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)					//claude가 정한 기본값
                .defaultHeader("x-api-key", secretKey)			//본인 키값
                .defaultHeader("anthropic-version", "2023-06-01")	//claude가 정한 기본값
                .defaultHeader("content-type", "application/json")	//claude가 정한 기본값
                .build();
    }
}