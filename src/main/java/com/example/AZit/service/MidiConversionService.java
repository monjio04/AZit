package com.example.AZit.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

@Service
public class MidiConversionService {

    private final WebClient webClient;

    public MidiConversionService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://127.0.0.1:8000") // FastAPI 서버 주소
                .defaultHeader("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
                .build();
    }

    /**
     * WAV 파일을 Python FastAPI 서버에 보내 MIDI 변환
     *
     * @param wavFilePath 변환할 WAV 파일 경로
     * @return 변환된 MIDI 파일 경로나 URL
     */
    public String convertWavToMidi(Path wavFilePath) {
        try {
            MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
            bodyMap.add("file", wavFilePath.toFile());

            Mono<String> response = webClient.post()
                    .uri("/convert") // FastAPI 엔드포인트
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(bodyMap)
                    .retrieve()
                    .bodyToMono(String.class);

            return response.block(); // 동기 호출
        } catch (WebClientResponseException e) {
            e.printStackTrace();
            throw new RuntimeException("Python 서버 호출 실패: " + e.getMessage());
        }
    }
}
