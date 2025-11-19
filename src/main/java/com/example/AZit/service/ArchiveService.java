package com.example.AZit.service;

import com.example.AZit.domain.Songs;
import com.example.AZit.dto.response.ArchiveAllResponse;
import com.example.AZit.dto.response.ArchiveDetailResponse;
import com.example.AZit.repository.ElementsRepository;
import com.example.AZit.repository.SongsRepository;
import com.example.AZit.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveService {
    private final UserRepository userRepository;
    private final ElementsRepository elementsRepository;
    private final SongsRepository songsRepository;

    private final S3Presigner s3Presigner;
    @Value("${cloud.aws.s3.bucket-name}")
    private String bucket;

    public List<ArchiveAllResponse> getAllArchive() {

        List<Songs> songsList = songsRepository.findAllWithUserAndElementAndKeywords();

        if (songsList == null || songsList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다.");
        }

        return songsList.stream()
                .map(this::toArchiveAllResponse)
                .collect(Collectors.toList());
    }


    public ArchiveDetailResponse getDetailArchive(Long songId) {

        Songs song = songsRepository.findDetailById(songId)
                .orElseThrow(()-> new IllegalArgumentException("아카이브 데이터를 찾을 수 없습니다."));

        String presignedUrl = generatePresignedUrl(songId);

        return toArchiveDetailResponse(song,presignedUrl);
    }

    private ArchiveAllResponse toArchiveAllResponse(Songs s) {
        String presignedUrl = generatePresignedUrl(s.getId());

        return new ArchiveAllResponse(
                s.getId(),
                new ArchiveAllResponse.ArchiveUserDto(
                        s.getElement().getMemory().getUsers().getId(),
                        s.getElement().getMemory().getUsers().getNickName()
                ),
                new ArchiveAllResponse.ArchiveElementDto(
                        s.getElement().getId(),
                        s.getElement().getKeywords()
                ),
                presignedUrl
        );
    }



    private ArchiveDetailResponse toArchiveDetailResponse(Songs s,String presignedUrl) {
        return ArchiveDetailResponse.builder()
                .songId(s.getId())
                .user(new ArchiveDetailResponse.ArchiveUserDto(
                        s.getElement().getMemory().getUsers().getId(),
                        s.getElement().getMemory().getUsers().getNickName()
                ))
                .memory(new ArchiveDetailResponse.ArchiveMemoryDto(
                        s.getElement().getMemory().getAnswer1(),
                        s.getElement().getMemory().getAnswer2(),
                        s.getElement().getMemory().getAnswer3()
                ))
                .svgUrl(presignedUrl)
                .keywords(s.getElement().getKeywords())
                .build();
    }

        public String generatePresignedUrl (Long songId){
            // 1. S3 Key (경로)
            String s3Key = "song/" + songId + "/music.svg";

            // 2. 다운로드 파일명 설정 (AZit_Music_번호.svg)
            String downloadFileName = "AZit_Music_" + songId + ".svg";

            String contentDisposition = "attachment; filename=\"";
            try {
                // 한글/특수문자 깨짐 방지 인코딩
                String encodedName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8)
                        .replaceAll("\\+", "%20");
                contentDisposition += encodedName + "\"";
            } catch (Exception e) {
                contentDisposition += "download.svg\"";
            }

            // 3. 요청 객체 생성 (다운로드 헤더 추가됨)
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .responseContentDisposition(contentDisposition) // 핵심!
                    .build();

            // 4. URL 생성 (5분 유효)
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .getObjectRequest(getObjectRequest)
                    .build();

            // 5. 결과 반환
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        }
}
