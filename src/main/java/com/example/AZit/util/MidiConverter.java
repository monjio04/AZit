package com.example.AZit.util;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class MidiConverter {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8000") // FastAPI 서버
            .build();

    public Path convertToMidi(String wavPath) throws Exception {
        Path inputPath = Paths.get(wavPath);

        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(inputPath.toFile()));

        byte[] midiBytes = webClient.post()
                .uri("/convert-musicbox")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        if (midiBytes == null) {
            throw new RuntimeException("FastAPI에서 MIDI 변환 실패");
        }

        Path midiPath = inputPath.getParent().resolve(
                inputPath.getFileName().toString().replace(".wav", "_converted.mid")
        );
        Files.write(midiPath, midiBytes);

        return midiPath;
    }
}
