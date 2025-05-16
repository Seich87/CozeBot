package com.chatassist.cozetalk.domain.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PaymentRequest {
    private Amount amount;
    private Boolean capture;
    private String description;
    private com.chatassist.cozetalk.domain.dto.Confirmation confirmation;

    @JsonProperty("metadata")
    private Map<String, String> metadata;
}