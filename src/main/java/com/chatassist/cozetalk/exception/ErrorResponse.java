package com.chatassist.cozetalk.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель ответа с ошибкой для API.
 * Используется для унифицированного формата ошибок
 * при ответе на HTTP-запросы.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Статус ошибки HTTP (например, 400, 404, 500)
     */
    private int status;

    /**
     * Код ошибки для программной обработки
     */
    private String errorCode;

    /**
     * Сообщение об ошибке для отображения пользователю
     */
    private String message;

    /**
     * Время возникновения ошибки
     */
    private LocalDateTime timestamp;

    /**
     * Путь запроса, вызвавшего ошибку
     */
    private String path;

    /**
     * Дополнительные детали ошибки (если имеются)
     */
    private String details;

    /**
     * Создает простую ошибку с сообщением и кодом ошибки.
     *
     * @param status HTTP статус
     * @param errorCode Код ошибки
     * @param message Сообщение
     * @return Новый объект ErrorResponse
     */
    public static ErrorResponse of(int status, String errorCode, String message) {
        return ErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Создает ошибку с полным набором параметров.
     *
     * @param status HTTP статус
     * @param errorCode Код ошибки
     * @param message Сообщение
     * @param path Путь запроса
     * @param details Детали ошибки
     * @return Новый объект ErrorResponse
     */
    public static ErrorResponse of(int status, String errorCode, String message, String path, String details) {
        return ErrorResponse.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .details(details)
                .build();
    }
}