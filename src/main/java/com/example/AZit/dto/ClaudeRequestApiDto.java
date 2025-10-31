package com.example.AZit.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Builder
@Data
public class ClaudeRequestApiDto {
    private String model;
    private List<Message> messages;
    private int max_tokens;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}