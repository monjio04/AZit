package com.example.AZit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusicElementsResponseDto {
    private List<String> keywords;
    private String mood;
    private String scale;
    private String tempo;
    private String atmosphere;
}
