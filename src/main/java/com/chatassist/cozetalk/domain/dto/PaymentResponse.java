package com.chatassist.cozetalk.domain.dto;

import java.time.ZonedDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PaymentResponse {
    private String id;
    private String status;
    private Amount amount;
    private String description;
    private Confirmation confirmation;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    @JsonProperty("metadata")
    private Map<String, String> metadata;

    @Data
    public static class Confirmation {
        private String type;

        @JsonProperty("confirmation_url")
        private String confirmationUrl;
    }
}