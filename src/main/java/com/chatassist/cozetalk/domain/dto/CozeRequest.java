package com.chatassist.cozetalk.domain.dto;

import lombok.Data;

@Data
public class CozeRequest {
    private String prompt;
    private Integer maxTokens;
    private Double temperature;
}