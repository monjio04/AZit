package com.example.AZit.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MusicGenResponse {
    private Long songId;
    private String midiUrl;
    private String wavUrl;
    private String svgUrl;
    private String createdAt;
}
