package com.example.AZit.dto;

import lombok.*;
import java.util.List;

@Data
public class ClaudeResponseApiDto {
    private List<Content> content;
    private String id;          // 고유아이디
    private String model;       // 모델명
    private String role;        // 보통의 경우 모두 "assistant"
    private String stop_reason;     // (답변문장이 중간에 끊긴 경우 원인이 들어감) ["end_turn"(응답완료) / "max_tokens"(최대 토큰 수에 도달) / "stop_sequence"(stop_sequence 발현)] or null
    private String stop_sequence;   // [null / request에서 설정했던 stop_sequence값 ]
    private String type;        // "message" or "error"
    private Usage usage;

    @Data
    public static class Content {
        public String text;     // 답변 내용
        public String type;     // "text" or error 정보
    }

    @Data
    public static class Usage {
        public int input_tokens;    //input 토큰 수
        public int output_tokens;   //output 토큰 수
    }

    // Claude 응답 텍스트만 가져오기
    public String getTextContent() {
        if (content != null && !content.isEmpty()) {
            return content.get(0).getText();
        }
        return "";
    }

    //error 답변인 경우 해당 함수 호출
    public static ClaudeResponseApiDto getClaudeErrorDto(String errorMessage) {
        ClaudeResponseApiDto claudeResponseApiDto = new ClaudeResponseApiDto();
        Content content = new Content();
        content.setText(errorMessage);
        claudeResponseApiDto.setType("error");
        claudeResponseApiDto.setContent(List.of(content));
        return claudeResponseApiDto;
    }
}