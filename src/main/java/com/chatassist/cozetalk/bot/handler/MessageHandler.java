package com.chatassist.cozetalk.bot.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.chatassist.cozetalk.bot.keyboard.InlineKeyboardFactory;
import com.chatassist.cozetalk.domain.dto.CozeResponse;
import com.chatassist.cozetalk.service.CozeService;
import com.chatassist.cozetalk.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageHandler {

    private final TelegramLongPollingBot bot;
    private final CozeService cozeService;
    private final SubscriptionService subscriptionService;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    public void handleMessage(Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        String text = message.getText();

        // Проверка, имеет ли пользователь активную подписку с доступными запросами
        if (!subscriptionService.canUserMakeRequest(userId)) {
            sendSubscriptionLimitMessage(chatId);
            return;
        }

        // Отправляем "набирает сообщение..." чтобы пользователь знал, что запрос обрабатывается
        sendTypingAction(chatId);

        // Обработка запроса через Coze API
        cozeService.processQuery(userId, text)
                .subscribe(
                        response -> sendResponse(chatId, response),
                        error -> {
                            log.error("Ошибка обработки запроса: {}", error.getMessage(), error);
                            sendErrorMessage(chatId);
                        }
                );
    }

    private void sendResponse(Long chatId, CozeResponse response) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(response.getContent());
        message.enableMarkdown(true);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки ответа: {}", e.getMessage(), e);
        }
    }

    private void sendSubscriptionLimitMessage(Long chatId) {
        String text = "⚠️ *Лимит запросов исчерпан или ваша подписка истекла*\n\n"
                + "Для продолжения использования бота, пожалуйста, выберите или обновите тарифный план.";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");
        message.setReplyMarkup(inlineKeyboardFactory.createTariffKeyboard());

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения о лимите подписки: {}", e.getMessage(), e);
        }
    }

    private void sendErrorMessage(Long chatId) {
        String text = "Извините, произошла ошибка при обработке вашего запроса. Пожалуйста, попробуйте позже.";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения об ошибке: {}", e.getMessage(), e);
        }
    }

    private void sendTypingAction(Long chatId) {
        try {
            bot.execute(new SendChatAction()
                    .setChatId(chatId)
                    .setAction(org.telegram.telegrambots.meta.api.objects.ChatAction.TYPING));
        } catch (TelegramApiException e) {
            log.warn("Ошибка отправки статуса набора текста: {}", e.getMessage());
        }
    }
}