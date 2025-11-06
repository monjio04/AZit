package com.example.AZit.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class SvgConverter {

    private final WebClient webClient;

    public SvgConverter(@Value("${fastapi.base-url}") String fastApiBaseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(fastApiBaseUrl) // 3. 하드코딩된 URL 대신 변수를 사용합니다.
                .build();
    }

    public Path convertToSvg(Path midiPath) throws Exception {
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(midiPath.toFile()));

        byte[] svgBytes = webClient.post()
                .uri("/convert-musicbox") // FastAPI SVG 변환 엔드포인트
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                // ★★★ 오류 핸들러 추가 ★★★
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.println("SvgConverter FastAPI Error: " + errorBody);
                                    // "가짜 MIDI" 오류 등이 여기에 잡힙니다.
                                    return Mono.error(new RuntimeException("FastAPI (MIDI->SVG) 변환 실패: " + errorBody));
                                })
                )
                .bodyToMono(byte[].class)
                .block(); // block()은 예외를 여기서 던집니다.

        if (svgBytes == null) throw new RuntimeException("FastAPI에서 SVG 변환 실패 (null 반환)");

        // ★ 임시 파일 확장자를 .svg로 변경
        Path svgPath = Files.createTempFile("converted_", ".svg");
        Files.write(svgPath, svgBytes);
        return svgPath;
    }
}