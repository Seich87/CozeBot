package com.chatassist.cozetalk.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CozeResponse {
    private String id;
    private String content;

    @JsonProperty("token_usage")
    private TokenUsage tokenUsage;

    @Data
    public static class TokenUsage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}