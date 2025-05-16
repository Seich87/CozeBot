package com.chatassist.cozetalk.exception;

/**
 * Исключение, связанное с общими ошибками в работе Telegram бота.
 * Используется для обработки нестандартных ситуаций при взаимодействии с пользователями.
 */
public class BotException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message Сообщение об ошибке
     */
    public BotException(String message) {
        super(message);
    }

    /**
     * Создает новое исключение с указанным сообщением и причиной.
     *
     * @param message Сообщение об ошибке
     * @param cause Исходная причина исключения
     */
    public BotException(String message, Throwable cause) {
        super(message, cause);
    }
}