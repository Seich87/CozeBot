package com.chatassist.cozetalk.bot.handler;

import com.chatassist.cozetalk.domain.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.chatassist.cozetalk.bot.keyboard.InlineKeyboardFactory;
import com.chatassist.cozetalk.bot.keyboard.ReplyKeyboardFactory;
import com.chatassist.cozetalk.domain.Subscription;
import com.chatassist.cozetalk.service.PaymentService;
import com.chatassist.cozetalk.service.SubscriptionService;
import com.chatassist.cozetalk.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandHandler {

    private final TelegramLongPollingBot bot;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    private final ReplyKeyboardFactory replyKeyboardFactory;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void handleCommand(Message message) {
        String command = message.getText();
        Long chatId = message.getChatId();

        switch (command.split(" ")[0]) {
            case "/start":
                handleStartCommand(chatId);
                break;
            case "/help":
                handleHelpCommand(chatId);
                break;
            case "/tariff":
                handleTariffCommand(chatId);
                break;
            case "/profile":
                handleProfileCommand(chatId, message.getFrom().getId());
                break;
            default:
                handleUnknownCommand(chatId);
                break;
        }
    }

    private void handleStartCommand(Long chatId) {
        String welcomeText = "Добро пожаловать в CozeTalk! 👋\n\n"
                + "Я готов помочь вам с вашими запросами, используя нейромодель Coze API.\n\n"
                + "🔹 Отправьте мне любой вопрос или запрос.\n"
                + "🔹 Используйте /tariff для выбора тарифного плана.\n"
                + "🔹 Используйте /profile для просмотра информации о вашем профиле.\n"
                + "🔹 Используйте /help чтобы увидеть все доступные команды.";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(welcomeText);
        message.setReplyMarkup(replyKeyboardFactory.createMainMenuKeyboard());

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки приветственного сообщения: {}", e.getMessage(), e);
        }
    }

    private void handleHelpCommand(Long chatId) {
        String helpText = "Доступные команды:\n\n"
                + "🔹 /start - начать использование бота\n"
                + "🔹 /help - показать список команд\n"
                + "🔹 /tariff - выбрать тарифный план\n"
                + "🔹 /profile - информация о вашем профиле\n\n"
                + "Просто отправьте мне любой текстовый запрос, и я отвечу вам с помощью нейромодели Coze API.";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(helpText);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения с помощью: {}", e.getMessage(), e);
        }
    }

    private void handleTariffCommand(Long chatId) {
        String tariffText = "Выберите тарифный план:\n\n"
                + "🔹 *Романтик* - 50 запросов в день - 990 ₽/месяц\n"
                + "🔹 *Альфач* - 150 запросов в день - 1990 ₽/месяц\n"
                + "🔹 *Ловелас* - Безлимитные запросы - 4990 ₽/месяц\n\n"
                + "Выберите тариф ниже:";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(tariffText);
        message.setParseMode("Markdown");
        message.setReplyMarkup(inlineKeyboardFactory.createTariffKeyboard());

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения с тарифами: {}", e.getMessage(), e);
        }
    }

    private void handleProfileCommand(Long chatId, Long telegramId) {
        // Получаем информацию о пользователе и его подписке
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Optional<Subscription> subscriptionOpt = subscriptionService.findByUser(user);

        StringBuilder profileText = new StringBuilder();
        profileText.append("*Ваш профиль*\n\n");
        profileText.append("🔹 ID: ").append(user.getTelegramId()).append("\n");
        profileText.append("🔹 Имя: ").append(user.getFirstName()).append("\n");

        if (subscriptionOpt.isPresent()) {
            Subscription subscription = subscriptionOpt.get();

            profileText.append("\n*Информация о подписке*\n\n");
            profileText.append("🔹 Тариф: *").append(subscription.getTariffPlan()).append("*\n");
            profileText.append("🔹 Дневной лимит: ").append(subscription.getDailyLimit()).append(" запросов\n");
            profileText.append("🔹 Осталось сегодня: ")
                    .append(subscriptionService.getRemainingRequests(telegramId)).append(" запросов\n");
            profileText.append("🔹 Действует до: ")
                    .append(subscription.getEndDate().format(DATE_FORMATTER)).append("\n");

            LocalDateTime now = LocalDateTime.now();
            if (subscription.getEndDate().isBefore(now)) {
                profileText.append("\n⚠️ *Ваша подписка истекла!* Выберите новый тариф с помощью /tariff\n");
            }
        } else {
            profileText.append("\n⚠️ *У вас нет активной подписки*\n");
            profileText.append("Выберите тарифный план с помощью /tariff\n");
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(profileText.toString());
        message.setParseMode("Markdown");

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки информации о профиле: {}", e.getMessage(), e);
        }
    }

    private void handleUnknownCommand(Long chatId) {
        String text = "Неизвестная команда. Используйте /help для просмотра доступных команд.";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения о неизвестной команде: {}", e.getMessage(), e);
        }
    }
}