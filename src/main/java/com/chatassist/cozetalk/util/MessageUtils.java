package com.chatassist.cozetalk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Утилитный класс для работы с сообщениями в Telegram.
 * Предоставляет методы для отправки различных типов сообщений,
 * разделения длинных сообщений, управления клавиатурами и т.д.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageUtils {

    private final TelegramLongPollingBot bot;

    // Максимальная длина сообщения в Telegram (4096 символов)
    private static final int MAX_MESSAGE_LENGTH = 4096;

    /**
     * Отправляет текстовое сообщение пользователю.
     *
     * @param chatId ID чата
     * @param text Текст сообщения
     * @return Отправленное сообщение
     */
    public Message sendMessage(Long chatId, String text) {
        return sendMessage(chatId, text, null);
    }

    /**
     * Отправляет текстовое сообщение с клавиатурой.
     *
     * @param chatId ID чата
     * @param text Текст сообщения
     * @param replyKeyboard Клавиатура
     * @return Отправленное сообщение
     */
    public Message sendMessage(Long chatId, String text, ReplyKeyboard replyKeyboard) {
        if (text == null || text.isEmpty()) {
            log.warn("Attempted to send empty message to chat {}", chatId);
            return null;
        }

        // Проверяем длину сообщения
        if (text.length() > MAX_MESSAGE_LENGTH) {
            return sendLongMessage(chatId, text, replyKeyboard);
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        if (replyKeyboard != null) {
            message.setReplyMarkup(replyKeyboard);
        }

        try {
            return bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to chat {}: {}", chatId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Отправляет длинное сообщение разбивая его на части.
     *
     * @param chatId ID чата
     * @param text Длинный текст сообщения
     * @param replyKeyboard Клавиатура (будет прикреплена только к последней части)
     * @return Последнее отправленное сообщение
     */
    public Message sendLongMessage(Long chatId, String text, ReplyKeyboard replyKeyboard) {
        List<String> parts = splitMessage(text);
        Message lastMessage = null;

        for (int i = 0; i < parts.size(); i++) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(parts.get(i));

            // Клавиатуру прикрепляем только к последней части
            if (i == parts.size() - 1 && replyKeyboard != null) {
                message.setReplyMarkup(replyKeyboard);
            }

            try {
                lastMessage = bot.execute(message);
            } catch (TelegramApiException e) {
                log.error("Error sending part {} of long message to chat {}: {}",
                        i + 1, chatId, e.getMessage(), e);
            }
        }

        return lastMessage;
    }

    /**
     * Разбивает длинное сообщение на части, не превышающие максимальную длину.
     *
     * @param text Исходный текст
     * @return Список частей сообщения
     */
    private List<String> splitMessage(String text) {
        List<String> parts = new ArrayList<>();

        int startIndex = 0;
        while (startIndex < text.length()) {
            int endIndex = Math.min(startIndex + MAX_MESSAGE_LENGTH, text.length());

            // Если мы не в конце текста, ищем ближайший перенос строки для более
            // естественного разбиения
            if (endIndex < text.length()) {
                int newlineIndex = text.lastIndexOf("\n", endIndex);
                if (newlineIndex > startIndex) {
                    endIndex = newlineIndex + 1;
                }
            }

            parts.add(text.substring(startIndex, endIndex));
            startIndex = endIndex;
        }

        return parts;
    }

    /**
     * Отправляет сообщение с Markdown форматированием.
     *
     * @param chatId ID чата
     * @param text Текст сообщения в формате Markdown
     * @param replyKeyboard Клавиатура
     * @return Отправленное сообщение
     */
    public Message sendMarkdownMessage(Long chatId, String text, ReplyKeyboard replyKeyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode(ParseMode.MARKDOWN);

        if (replyKeyboard != null) {
            message.setReplyMarkup(replyKeyboard);
        }

        try {
            return bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending markdown message to chat {}: {}", chatId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Отправляет фотографию с подписью.
     *
     * @param chatId ID чата
     * @param photoUrl URL или файловый идентификатор фото
     * @param caption Подпись к фото
     * @return Отправленное сообщение
     */
    public Message sendPhoto(Long chatId, String photoUrl, String caption) {
        return sendPhoto(chatId, photoUrl, caption, null);
    }

    /**
     * Отправляет фотографию с подписью и клавиатурой.
     *
     * @param chatId ID чата
     * @param photoUrl URL или файловый идентификатор фото
     * @param caption Подпись к фото
     * @param replyKeyboard Клавиатура
     * @return Отправленное сообщение
     */
    public Message sendPhoto(Long chatId, String photoUrl, String caption, ReplyKeyboard replyKeyboard) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(photoUrl));

        if (caption != null && !caption.isEmpty()) {
            photo.setCaption(caption);
        }

        if (replyKeyboard != null) {
            photo.setReplyMarkup(replyKeyboard);
        }

        try {
            return bot.execute(photo);
        } catch (TelegramApiException e) {
            log.error("Error sending photo to chat {}: {}", chatId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Отправляет GIF-анимацию с подписью.
     *
     * @param chatId ID чата
     * @param animationUrl URL или файловый идентификатор анимации
     * @param caption Подпись к анимации
     * @return Отправленное сообщение
     */
    public Message sendAnimation(Long chatId, String animationUrl, String caption) {
        SendAnimation animation = new SendAnimation();
        animation.setChatId(chatId);
        animation.setAnimation(new InputFile(animationUrl));

        if (caption != null && !caption.isEmpty()) {
            animation.setCaption(caption);
        }

        try {
            return bot.execute(animation);
        } catch (TelegramApiException e) {
            log.error("Error sending animation to chat {}: {}", chatId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Редактирует ранее отправленное сообщение.
     *
     * @param chatId ID чата
     * @param messageId ID сообщения для редактирования
     * @param newText Новый текст сообщения
     * @return true, если редактирование успешно
     */
    public boolean editMessage(Long chatId, Integer messageId, String newText) {
        return editMessage(chatId, messageId, newText, null);
    }

    /**
     * Редактирует ранее отправленное сообщение с обновлением клавиатуры.
     *
     * @param chatId ID чата
     * @param messageId ID сообщения для редактирования
     * @param newText Новый текст сообщения
     * @param inlineKeyboard Новая Inline-клавиатура
     * @return true, если редактирование успешно
     */
    public boolean editMessage(Long chatId, Integer messageId, String newText, InlineKeyboardMarkup inlineKeyboard) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(newText);

        if (inlineKeyboard != null) {
            editMessage.setReplyMarkup(inlineKeyboard);
        }

        try {
            bot.execute(editMessage);
            return true;
        } catch (TelegramApiException e) {
            log.error("Error editing message {} in chat {}: {}", messageId, chatId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Удаляет сообщение.
     *
     * @param chatId ID чата
     * @param messageId ID сообщения для удаления
     * @return true, если удаление успешно
     */
    public boolean deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);

        try {
            return bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Error deleting message {} in chat {}: {}", messageId, chatId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Асинхронно отправляет сообщение пользователю.
     *
     * @param chatId ID чата
     * @param text Текст сообщения
     * @return CompletableFuture с отправленным сообщением
     */
    public CompletableFuture<Message> sendMessageAsync(Long chatId, String text) {
        return CompletableFuture.supplyAsync(() -> sendMessage(chatId, text));
    }

    /**
     * Форматирует сообщение об ошибке.
     *
     * @param errorMessage Текст ошибки
     * @return Отформатированный текст ошибки
     */
    public String formatErrorMessage(String errorMessage) {
        return "⚠️ *Ошибка*\n\n" + errorMessage;
    }

    /**
     * Форматирует сообщение об успешной операции.
     *
     * @param successMessage Текст сообщения
     * @return Отформатированный текст успешной операции
     */
    public String formatSuccessMessage(String successMessage) {
        return "✅ *Успешно*\n\n" + successMessage;
    }

    /**
     * Форматирует сообщение об информации.
     *
     * @param infoMessage Текст информационного сообщения
     * @return Отформатированный текст информационного сообщения
     */
    public String formatInfoMessage(String infoMessage) {
        return "ℹ️ *Информация*\n\n" + infoMessage;
    }
}