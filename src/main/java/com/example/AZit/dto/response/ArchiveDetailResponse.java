package com.example.AZit.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ArchiveDetailResponse {
    private Long songId;
    private ArchiveUserDto user;
    private ArchiveMemoryDto memory;
    private String svgUrl;
    private List<String> keywords;


    @Getter
    @Builder
    @AllArgsConstructor
    public static class ArchiveUserDto{
        private Long userId;
        private String userName;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ArchiveMemoryDto{
        private String answer1;
        private String answer2;
        private String answer3;
    }
}
