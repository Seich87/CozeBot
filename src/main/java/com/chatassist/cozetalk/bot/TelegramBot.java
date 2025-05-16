package com.chatassist.cozetalk.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.chatassist.cozetalk.bot.handler.CallbackQueryHandler;
import com.chatassist.cozetalk.bot.handler.CommandHandler;
import com.chatassist.cozetalk.bot.handler.MessageHandler;
import com.chatassist.cozetalk.config.BotConfig;
import com.chatassist.cozetalk.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final CommandHandler commandHandler;
    private final MessageHandler messageHandler;
    private final CallbackQueryHandler callbackQueryHandler;
    private final UserService userService;

    public TelegramBot(BotConfig botConfig,
                       CommandHandler commandHandler,
                       MessageHandler messageHandler,
                       CallbackQueryHandler callbackQueryHandler,
                       UserService userService) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.commandHandler = commandHandler;
        this.messageHandler = messageHandler;
        this.callbackQueryHandler = callbackQueryHandler;
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error("Ошибка обработки обновления: {}", e.getMessage(), e);
            sendErrorMessage(update);
        }
    }

    private void handleTextMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        // Регистрация пользователя, если первый раз
        userService.registerUserIfNotExists(message.getFrom());

        if (text.startsWith("/")) {
            commandHandler.handleCommand(message);
        } else {
            messageHandler.handleMessage(message);
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        callbackQueryHandler.handleCallbackQuery(callbackQuery);
    }

    private void sendErrorMessage(Update update) {
        Long chatId;
        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Произошла ошибка при обработке вашего запроса. Пожалуйста, попробуйте позже.");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение об ошибке: {}", e.getMessage(), e);
        }
    }
}