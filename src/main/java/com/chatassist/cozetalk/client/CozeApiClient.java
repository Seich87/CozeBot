package com.chatassist.cozetalk.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.chatassist.cozetalk.domain.dto.CozeRequest;
import com.chatassist.cozetalk.domain.dto.CozeResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class CozeApiClient {

    private final WebClient webClient;
    private final String apiKey;

    public CozeApiClient(WebClient webClient,
                         @Value("${coze.api.base-url}") String baseUrl,
                         @Value("${coze.api.key}") String apiKey) {
        this.webClient = webClient.mutate()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.apiKey = apiKey;
    }

    public Mono<CozeResponse> sendMessage(CozeRequest request) {
        return webClient.post()
                .uri("/api/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CozeResponse.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                        .filter(throwable -> !throwable.getMessage().contains("400")))
                .doOnError(e -> log.error("Ошибка при вызове Coze API: {}", e.getMessage(), e));
    }
}