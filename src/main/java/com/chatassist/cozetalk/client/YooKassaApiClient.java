package com.chatassist.cozetalk.client;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.chatassist.cozetalk.domain.dto.PaymentRequest;
import com.chatassist.cozetalk.domain.dto.PaymentResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@Slf4j
public class YooKassaApiClient {

    private final WebClient webClient;
    private final String shopId;
    private final String secretKey;

    public YooKassaApiClient(WebClient webClient,
                             @Value("${yukassa.api.base-url}") String baseUrl,
                             @Value("${yukassa.shop-id}") String shopId,
                             @Value("${yukassa.secret-key}") String secretKey) {
        this.webClient = webClient.mutate()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "Basic " + Base64.getEncoder().encodeToString(
                                (shopId + ":" + secretKey).getBytes(StandardCharsets.UTF_8)))
                .build();
        this.shopId = shopId;
        this.secretKey = secretKey;
    }

    public Mono<PaymentResponse> createPayment(PaymentRequest request) {
        return webClient.post()
                .uri("/v3/payments")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(1))
                        .filter(throwable -> !throwable.getMessage().contains("400")))
                .doOnError(e -> log.error("Ошибка создания платежа ЮKassa: {}", e.getMessage(), e));
    }

    public Mono<PaymentResponse> getPayment(String paymentId) {
        return webClient.get()
                .uri("/v3/payments/{paymentId}", paymentId)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(1)))
                .doOnError(e -> log.error("Ошибка получения платежа ЮKassa: {}", e.getMessage(), e));
    }
}