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
                "Generate a monophonic **music-box (orgel) style** melody. " +
                        "Mood: %s. Scale: %s (strictly diatonic). Tempo: %s. Atmosphere: %s. " +
                        "Use only the pitch range **C4–C6**. Do NOT use accidentals (# or b). " +
                        "Instrument: music box / piano-like plucked tone. " +
                        "Texture: strictly one note at a time (no chords). " +
                        "Melody style: simple, repetitive, lullaby-like, suitable for a mechanical music box. " +
                        "Duration: ~10 seconds. " +
                        "Keywords: %s. ,Make music that matches the keyword" +
                        "Output the melody clearly structured for generation.",
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
        Songs songs = Songs.builder()
                .element(elements)
                .createdAt(LocalDateTime.now())
                .build();

        Songs savedSong = songsRepository.save(songs); // 여기서 ID가 생성됨!
        Long realSongId = savedSong.getId();

        String wavS3 = ncpStorageService.uploadFile("song/" + realSongId + "/music.wav", wavPath);
        String midiS3 = ncpStorageService.uploadFile("song/" + realSongId + "/music.mid", midiPath);
        String svgS3 = ncpStorageService.uploadFile("song/" + realSongId + "/music.svg", svgPath);


        savedSong.setWavUrl(wavS3);
        savedSong.setMidiUrl(midiS3);
        savedSong.setSvgUrl(svgS3);

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
