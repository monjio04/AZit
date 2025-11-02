package com.example.AZit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MemoryCreateResponse {
    private Long memoryId;
    private LocalDateTime createdAt;
}
