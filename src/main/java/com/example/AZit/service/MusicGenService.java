package com.example.AZit.service;

import com.example.AZit.config.ReplicateConfig;
import com.example.AZit.domain.Elements;
import com.example.AZit.domain.Songs;
import com.example.AZit.dto.response.MusicGenResponse;
import com.example.AZit.repository.ElementsRepository;
import com.example.AZit.repository.SongsRepository;
import com.example.AZit.util.FileDownloader;
import com.example.AZit.util.MidiConverter;
import com.example.AZit.util.SvgConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Path;
import java.time.LocalDateTime;
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
    private final ElementsRepository elementsRepository;
    private final AwsS3Service ncpStorageService;
    private final SongsRepository songsRepository;
    private final WebClient replicateWebClient;
    private final FileDownloader fileDownloader;
    private final MidiConverter midiConverter;
    private final SvgConverter svgConverter;


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

    public MusicGenResponse createSong(Long elementId) throws Exception{
        Elements elements = elementsRepository.findById(elementId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 elementId입니다."));

        String prompt = String.format(
                "A %s song in %s, %s tempo. %s atmosphere, Monophonic, with no accidentals texture. " +
                        "instruments: piano. Pitch range: C4-C6. " +
                        "Keywords: %s. Duration: 10s.",
                elements.getMood(),
                elements.getScale(),
                elements.getTempo(),
                elements.getAtmosphere(),
                String.join(", ", elements.getKeywords())
        );

        String wavUrl = generateMusicAndGetUrl(prompt);

        Path wavPath = fileDownloader.downloadFile(wavUrl);

        Path midiPath=midiConverter.convertToMidi(wavPath);

        Path svgPath=svgConverter.convertToSvg(midiPath);

        //스토리지에 업로드
        String wavS3 = ncpStorageService.uploadFile("song/" + elementId + "/music.wav", wavPath);
        String midiS3 = ncpStorageService.uploadFile("song/" + elementId + "/music.mid", midiPath);
        String svgS3 = ncpStorageService.uploadFile("song/" + elementId + "/music.svg", svgPath);

        Songs songs = Songs.builder()
                .element(elements)
                .wavUrl(wavS3)
                .midiUrl(midiS3)
                .svgUrl(svgS3)
                .createdAt(LocalDateTime.now())
                .build();

        songsRepository.save(songs);

        wavPath.toFile().delete();
        midiPath.toFile().delete();
        svgPath.toFile().delete();

        return MusicGenResponse.builder()
                .songId(songs.getId())
                .wavUrl(songs.getWavUrl())
                .midiUrl(songs.getMidiUrl())
                .svgUrl(songs.getSvgUrl())
                .build();
    }
}
