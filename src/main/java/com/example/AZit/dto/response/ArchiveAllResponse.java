package com.example.AZit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ArchiveAllResponse {
    private Long songId;
    private ArchiveUserDto user;
    private ArchiveElementDto element;
    private String svgUrl;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ArchiveUserDto{
        private Long userId;
        private String nickName;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ArchiveElementDto{
        private Long elementId;
        private List<String> keywords;
    }
}
