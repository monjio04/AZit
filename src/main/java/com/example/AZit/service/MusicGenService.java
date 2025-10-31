package com.example.AZit.service;

import com.example.AZit.config.ReplicateConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MusicGenService {

    private final ReplicateConfig config;
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.replicate.com/v1")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public String generateMusicAndGetUrl(String prompt) throws InterruptedException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("version", "671ac645ce5e552cc63a54a2bbff63fcf798043055d2dac5fc9e36a837eedcfb");
        requestBody.put("input", Map.of("prompt", prompt));

        Map<String, Object> prediction = webClient.post()
                .uri("/predictions")
                .header("Authorization", "Token " + config.getToken())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String predictionId = (String) prediction.get("id");

        String outputUrl = null;
        for (int i = 0; i < 30; i++) {
            Map<String, Object> status = webClient.get()
                    .uri("/predictions/" + predictionId)
                    .header("Authorization", "Token " + config.getToken())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String currentStatus = (String) status.get("status");
            if ("succeeded".equals(currentStatus)) {
                Object outputObj = status.get("output");

                if (outputObj instanceof java.util.List<?> list) {
                    outputUrl = (String) list.get(0);
                } else if (outputObj instanceof String str) {
                    outputUrl = str;
                }
                break;
            } else if ("failed".equals(currentStatus)) {
                throw new RuntimeException("Music generation failed!");
            }

            Thread.sleep(3000);
        }

        return outputUrl;
    }
}
