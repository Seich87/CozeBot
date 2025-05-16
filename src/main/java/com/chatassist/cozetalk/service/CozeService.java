package com.chatassist.cozetalk.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatassist.cozetalk.client.CozeApiClient;
import com.chatassist.cozetalk.domain.RequestLog;
import com.chatassist.cozetalk.domain.User;
import com.chatassist.cozetalk.domain.dto.CozeRequest;
import com.chatassist.cozetalk.domain.dto.CozeResponse;
import com.chatassist.cozetalk.repository.RequestLogRepository;
import com.chatassist.cozetalk.repository.UserRepository;
import com.chatassist.cozetalk.exception.CozeApiException;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CozeService {

    private final CozeApiClient cozeApiClient;
    private final RequestLogRepository requestLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public Mono<CozeResponse> processQuery(Long telegramId, String query) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        // Создаем запись о запросе
        RequestLog requestLog = new RequestLog();
        requestLog.setUser(user);
        requestLog.setRequestText(query);
        requestLog.setStatus("PROCESSING");
        requestLogRepository.save(requestLog);

        Instant startTime = Instant.now();

        CozeRequest request = new CozeRequest();
        request.setPrompt(query);
        request.setMaxTokens(2048);
        request.setTemperature(0.7);

        return cozeApiClient.sendMessage(request)
                .doOnNext(response -> {
                    // Обновляем запись о запросе с результатом
                    requestLog.setResponseText(response.getContent());
                    requestLog.setStatus("SUCCESS");
                    requestLog.setProcessTime((int) Duration.between(startTime, Instant.now()).toMillis());
                    requestLogRepository.save(requestLog);
                })
                .doOnError(error -> {
                    // Обновляем запись о запросе с ошибкой
                    requestLog.setResponseText("Ошибка: " + error.getMessage());
                    requestLog.setStatus("ERROR");
                    requestLog.setProcessTime((int) Duration.between(startTime, Instant.now()).toMillis());
                    requestLogRepository.save(requestLog);

                    log.error("Ошибка при обработке запроса: {}", error.getMessage(), error);

                    throw new CozeApiException("Ошибка при обработке запроса: " + error.getMessage(), error);
                });
    }
}