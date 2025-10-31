package com.example.AZit.controller;

import com.example.AZit.service.MusicGenService;
import com.example.AZit.util.FileDownloader;
import com.example.AZit.util.MidiConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/replicate/musicgen")
@RequiredArgsConstructor
public class MusicGenController {

    private final MusicGenService musicGenService;
    private final FileDownloader fileDownloader;
    private final MidiConverter midiConverter;

    @PostMapping("/generate")
    public String generateMusic(@RequestBody Map<String, String> request) throws Exception {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return "❌ 프롬프트가 비어있습니다.";
        }

        // 1️⃣ MusicGen API 호출 → URL 가져오기
        String musicUrl = musicGenService.generateMusicAndGetUrl(prompt);
        if (musicUrl == null) {
            return "❌ 음악 생성 실패: URL을 가져오지 못했습니다.";
        }
        String safeFileName = prompt.replaceAll("[^a-zA-Z0-9_-]", "_");

        // 2️⃣ 로컬 MP3 파일 저장
        Path mp3Path = Paths.get("generated_music", safeFileName + ".mp3");
        fileDownloader.downloadFile(musicUrl, mp3Path.toString());

        // 3️⃣ MIDI 변환
        Path midiPath = midiConverter.convertToMidi(mp3Path.toString());

        return "✅ 음악 생성 완료!\nMP3 파일: " + mp3Path + "\nMIDI 파일: " + midiPath + "\n원본 URL: " + musicUrl;
    }
}
