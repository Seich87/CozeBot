package com.chatassist.cozetalk.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.chatassist.cozetalk.domain.RequestLog;
import com.chatassist.cozetalk.domain.User;
import com.chatassist.cozetalk.repository.RequestLogRepository;
import com.chatassist.cozetalk.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Утилитный класс для логирования работы бота и API.
 * Предоставляет методы для логирования запросов, ошибок,
 * и сохранения логов в базе данных.
 */
@Component
@RequiredArgsConstructor
public class LoggingUtils {

    private static final Logger logger = LoggerFactory.getLogger(LoggingUtils.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final RequestLogRepository requestLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Логирует входящее обновление от Telegram.
     *
     * @param update Обновление от Telegram API
     */
    public void logIncomingUpdate(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String username = update.getMessage().getFrom().getUserName();
                Long userId = update.getMessage().getFrom().getId();
                String text = update.getMessage().getText();

                logger.info("Received message from @{} (ID: {}): {}",
                        username != null ? username : "unknown",
                        userId,
                        text);
            } else if (update.hasCallbackQuery()) {
                String username = update.getCallbackQuery().getFrom().getUserName();
                Long userId = update.getCallbackQuery().getFrom().getId();
                String callbackData = update.getCallbackQuery().getData();

                logger.info("Received callback from @{} (ID: {}): {}",
                        username != null ? username : "unknown",
                        userId,
                        callbackData);
            }
        } catch (Exception e) {
            logger.error("Error logging update: {}", e.getMessage(), e);
        }
    }

    /**
     * Логирует запрос к Coze API и сохраняет его в базе данных.
     *
     * @param telegramId ID пользователя в Telegram
     * @param requestText Текст запроса
     * @return Созданный объект лога запроса
     */
    public RequestLog logCozeRequest(Long telegramId, String requestText) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElse(null);

        if (user == null) {
            logger.warn("Coze request from unknown user (ID: {}): {}", telegramId, requestText);
            return null;
        }

        RequestLog requestLog = new RequestLog();
        requestLog.setUser(user);
        requestLog.setRequestTime(LocalDateTime.now());
        requestLog.setRequestText(requestText);
        requestLog.setStatus("PROCESSING");

        logger.info("Coze request from user {} (ID: {}): {}",
                user.getUsername() != null ? user.getUsername() : "unknown",
                telegramId,
                requestText);

        return requestLogRepository.save(requestLog);
    }

    /**
     * Обновляет лог запроса с результатом ответа от Coze API.
     *
     * @param requestLog Существующий лог запроса
     * @param responseText Текст ответа
     * @param status Статус обработки
     * @param processTimeMillis Время обработки в миллисекундах
     * @return Обновленный объект лога запроса
     */
    public RequestLog updateRequestLog(RequestLog requestLog, String responseText, String status, int processTimeMillis) {
        if (requestLog == null) {
            return null;
        }

        requestLog.setResponseText(responseText);
        requestLog.setStatus(status);
        requestLog.setProcessTime(processTimeMillis);

        logger.info("Coze response for user {} (ID: {}), status: {}, time: {} ms",
                requestLog.getUser().getUsername() != null ? requestLog.getUser().getUsername() : "unknown",
                requestLog.getUser().getTelegramId(),
                status,
                processTimeMillis);

        return requestLogRepository.save(requestLog);
    }

    /**
     * Логирует ошибку с подробной информацией о стеке вызовов.
     *
     * @param message Сообщение об ошибке
     * @param exception Исключение
     */
    public void logError(String message, Throwable exception) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String stackTrace = Arrays.stream(exception.getStackTrace())
                .limit(10) // Ограничиваем до 10 строк стека для более компактного логирования
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n    "));

        logger.error("{} - {}: {} \nStack trace:\n    {}",
                timestamp,
                message,
                exception.getMessage(),
                stackTrace);
    }

    /**
     * Логирует информацию о платеже.
     *
     * @param telegramId ID пользователя в Telegram
     * @param action Действие с платежом (создание, обработка и т.д.)
     * @param paymentId ID платежа
     * @param details Дополнительные данные
     */
    public void logPayment(Long telegramId, String action, String paymentId, Object details) {
        String username = "unknown";

        User user = userRepository.findByTelegramId(telegramId).orElse(null);
        if (user != null && user.getUsername() != null) {
            username = user.getUsername();
        }

        String detailsStr = "";
        if (details != null) {
            try {
                detailsStr = objectMapper.writeValueAsString(details);
            } catch (Exception e) {
                detailsStr = details.toString();
            }
        }

        logger.info("Payment {} for user @{} (ID: {}), paymentId: {}, details: {}",
                action,
                username,
                telegramId,
                paymentId,
                detailsStr);
    }

    /**
     * Логирует критическую ошибку и уведомляет администратора.
     *
     * @param message Сообщение об ошибке
     * @param exception Исключение
     */
    public void logCriticalError(String message, Throwable exception) {
        logError("CRITICAL ERROR: " + message, exception);

        // Здесь может быть код для уведомления администратора
        // например, отправка email или сообщения в Telegram
    }

    /**
     * Логирует действие администратора.
     *
     * @param adminUsername Имя пользователя администратора
     * @param action Выполненное действие
     * @param details Детали действия
     */
    public void logAdminAction(String adminUsername, String action, String details) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);

        logger.info("{} - Admin action: {} performed by {}, details: {}",
                timestamp,
                action,
                adminUsername,
                details);
    }
}