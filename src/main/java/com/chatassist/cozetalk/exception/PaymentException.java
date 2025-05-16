package com.chatassist.cozetalk.exception;

/**
 * Исключение, связанное с ошибками в платежной системе.
 * Используется при проблемах с ЮKassa API, неудачных платежах
 * или других ошибках, связанных с процессом оплаты.
 */
public class PaymentException extends RuntimeException {

    /**
     * Идентификатор платежа, если имеется
     */
    private final String paymentId;

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param message Сообщение об ошибке
     */
    public PaymentException(String message) {
        super(message);
        this.paymentId = null;
    }

    /**
     * Создает новое исключение с указанным сообщением и идентификатором платежа.
     *
     * @param message Сообщение об ошибке
     * @param paymentId Идентификатор платежа
     */
    public PaymentException(String message, String paymentId) {
        super(message);
        this.paymentId = paymentId;
    }

    /**
     * Создает новое исключение с указанным сообщением, идентификатором платежа и причиной.
     *
     * @param message Сообщение об ошибке
     * @param paymentId Идентификатор платежа
     * @param cause Исходная причина исключения
     */
    public PaymentException(String message, String paymentId, Throwable cause) {
        super(message, cause);
        this.paymentId = paymentId;
    }

    /**
     * Создает новое исключение с указанным сообщением и причиной.
     *
     * @param message Сообщение об ошибке
     * @param cause Исходная причина исключения
     */
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.paymentId = null;
    }

    /**
     * Получает идентификатор платежа, связанный с ошибкой.
     *
     * @return Идентификатор платежа или null, если не задан
     */
    public String getPaymentId() {
        return paymentId;
    }
}