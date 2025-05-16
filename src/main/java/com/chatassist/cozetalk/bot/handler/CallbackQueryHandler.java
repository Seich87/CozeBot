package com.chatassist.cozetalk.bot.handler;

import com.chatassist.cozetalk.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.chatassist.cozetalk.domain.dto.PaymentResponse;
import com.chatassist.cozetalk.domain.enums.TariffPlan;
import com.chatassist.cozetalk.service.PaymentService;
import com.chatassist.cozetalk.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackQueryHandler {

    private final TelegramLongPollingBot bot;
    private final UserService userService;
    private final PaymentService paymentService;

    @Value("${yukassa.return-url}")
    private String returnUrl;

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();
        Long userId = callbackQuery.getFrom().getId();

        // Отвечаем на callback, чтобы убрать загрузку на кнопке
        answerCallbackQuery(callbackQuery.getId());

        if (callbackData.startsWith("tariff_")) {
            handleTariffSelection(chatId, userId, callbackData.substring(7));
        }
    }

    private void handleTariffSelection(Long chatId, Long telegramId, String tariffCode) {
        TariffPlan selectedTariff;
        try {
            selectedTariff = TariffPlan.valueOf(tariffCode);
        } catch (IllegalArgumentException e) {
            sendErrorMessage(chatId, "Неверный тарифный план. Пожалуйста, попробуйте снова.");
            return;
        }

        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Создаем платеж через ЮKassa
        paymentService.createPayment(user, selectedTariff, returnUrl)
                .subscribe(
                        response -> sendPaymentLink(chatId, response, selectedTariff),
                        error -> {
                            log.error("Ошибка создания платежа: {}", error.getMessage(), error);
                            sendErrorMessage(chatId, "Произошла ошибка при создании платежа. Пожалуйста, попробуйте позже.");
                        }
                );
    }

    private void sendPaymentLink(Long chatId, PaymentResponse paymentResponse, TariffPlan tariff) {
        String tariffName = switch (tariff) {
            case ROMANTIC -> "Романтик";
            case ALPHA -> "Альфач";
            case LOVELACE -> "Ловелас";
        };

        String text = String.format(
                "Вы выбрали тариф *%s*\n\n" +
                        "Стоимость: *%d ₽* за 1 месяц\n\n" +
                        "Для оплаты перейдите по ссылке ниже:\n" +
                        "[Оплатить](%s)\n\n" +
                        "После успешной оплаты ваш тариф будет автоматически активирован.",
                tariffName,
                tariff.getPriceInRubles(),
                paymentResponse.getConfirmation().getConfirmationUrl()
        );

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки ссылки на оплату: {}", e.getMessage(), e);
        }
    }

    private void sendErrorMessage(Long chatId, String errorText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(errorText);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения об ошибке: {}", e.getMessage(), e);
        }
    }

    private void answerCallbackQuery(String callbackId) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);

        try {
            bot.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Ошибка ответа на callback: {}", e.getMessage(), e);
        }
    }
}