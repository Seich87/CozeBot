package com.chatassist.cozetalk.exception;

/**
 * Исключение, связанное с ошибками при взаимодействии с Coze API.
 * Используется при проблемах с подключением, аутентификацией,
 * ограничениями API или ошибках обработки запросов.
 */
public class CozeApiException extends RuntimeException {

    /**
     * Код ошибки API, если имеется
     */
    private final String errorCode;

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message Сообщение об ошибке
     */
    public CozeApiException(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * Создает новое исключение с указанным сообщением и кодом ошибки.
     *
     * @param message Сообщение об ошибке
     * @param errorCode Код ошибки API
     */
    public CozeApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Создает новое исключение с указанным сообщением и причиной.
     *
     * @param message Сообщение об ошибке
     * @param cause Исходная причина исключения
     */
    public CozeApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    /**
     * Создает новое исключение с указанным сообщением, кодом ошибки и причиной.
     *
     * @param message Сообщение об ошибке
     * @param errorCode Код ошибки API
     * @param cause Исходная причина исключения
     */
    public CozeApiException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Получает код ошибки API, связанный с исключением.
     *
     * @return Код ошибки API или null, если не задан
     */
    public String getErrorCode() {
        return errorCode;
    }
}