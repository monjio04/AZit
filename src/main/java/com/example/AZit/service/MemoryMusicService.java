package com.example.AZit.service;

import com.example.AZit.domain.Elements;
import com.example.AZit.domain.Memory;
import com.example.AZit.dto.response.MusicElementsResponse;
import com.example.AZit.repository.ElementsRepository;
import com.example.AZit.repository.MemoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MemoryMusicService {

    private final MemoryRepository memoryRepository;
    private final ElementsRepository elementsRepository;
    private final ClaudeService claudeService;

    public Mono<MusicElementsResponse> analyzeMemory(Long memoryId) {

        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new IllegalArgumentException("메모리가 존재하지 않습니다."));

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
                """.formatted(memory.getAnswer1(), memory.getAnswer2(), memory.getAnswer3());

        return claudeService.sendApiRequest(prompt)
                .map(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        MusicElementsResponse elements = mapper.readValue(
                                response.getTextContent().trim(), MusicElementsResponse.class);

                        Elements element = Elements.builder()
                                .memory(memory)
                                .keywords(elements.getKeywords())
                                .mood(elements.getMood())
                                .scale(elements.getScale())
                                .tempo(elements.getTempo())
                                .atmosphere(elements.getAtmosphere())
                                .instruments("piano")
                                .length("10s")
                                .pitchRange("C4-C6")
                                .texture("Monophonic, with no accidentals")
                                .build();

                        elementsRepository.save(element);

                        return new MusicElementsResponse(
                                element.getId(),
                                elements.getKeywords(),
                                elements.getMood(),
                                elements.getScale(),
                                elements.getTempo(),
                                elements.getAtmosphere()
                        );

                    } catch (Exception e) {
                        throw new RuntimeException("Parsing Error: " + e.getMessage());
                    }
                });
    }
}
