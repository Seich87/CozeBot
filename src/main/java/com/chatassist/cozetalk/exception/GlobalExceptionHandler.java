package com.chatassist.cozetalk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Глобальный обработчик исключений для всего приложения.
 * Перехватывает различные типы исключений и преобразует их в унифицированные
 * HTTP-ответы с объектами ErrorResponse.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключения BotException.
     *
     * @param ex Исключение
     * @param request Текущий веб-запрос
     * @return ResponseEntity с информацией об ошибке
     */
    @ExceptionHandler(BotException.class)
    public ResponseEntity<ErrorResponse> handleBotException(BotException ex, WebRequest request) {
        log.error("Ошибка бота: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "BOT_ERROR",
                ex.getMessage(),
                getRequestPath(request),
                ex.getCause() != null ? ex.getCause().getMessage() : null
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Обрабатывает исключения PaymentException.
     *
     * @param ex Исключение
     * @param request Текущий веб-запрос
     * @return ResponseEntity с информацией об ошибке
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex, WebRequest request) {
        log.error("Ошибка платежа: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "PAYMENT_ERROR",
                ex.getMessage(),
                getRequestPath(request),
                "PaymentId: " + ex.getPaymentId()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключения CozeApiException.
     *
     * @param ex Исключение
     * @param request Текущий веб-запрос
     * @return ResponseEntity с информацией об ошибке
     */
    @ExceptionHandler(CozeApiException.class)
    public ResponseEntity<ErrorResponse> handleCozeApiException(CozeApiException ex, WebRequest request) {
        log.error("Ошибка Coze API: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "COZE_API_ERROR",
                "Ошибка при взаимодействии с Coze API",
                getRequestPath(request),
                ex.getMessage() + (ex.getErrorCode() != null ? " (Code: " + ex.getErrorCode() + ")" : "")
        );

        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Обрабатывает исключение jakarta.persistence.EntityNotFoundException.
     *
     * @param ex Исключение
     * @param request Текущий веб-запрос
     * @return ResponseEntity с информацией об ошибке
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            jakarta.persistence.EntityNotFoundException ex, WebRequest request) {
        log.error("Сущность не найдена: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "ENTITY_NOT_FOUND",
                ex.getMessage(),
                getRequestPath(request),
                null
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Обрабатывает все остальные исключения, не перехваченные другими обработчиками.
     *
     * @param ex Исключение
     * @param request Текущий веб-запрос
     * @return ResponseEntity с информацией об ошибке
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Необработанное исключение: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Внутренняя ошибка сервера",
                getRequestPath(request),
                ex.getMessage()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Извлекает путь запроса из объекта WebRequest.
     *
     * @param request Текущий веб-запрос
     * @return Путь запроса или null, если не может быть получен
     */
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return null;
    }
}