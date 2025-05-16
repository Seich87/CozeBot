package com.chatassist.cozetalk.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.chatassist.cozetalk.bot.TelegramBot;
import com.chatassist.cozetalk.config.BotConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@RestController
@RequestMapping("/bot")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final TelegramBot telegramBot;
    private final BotConfig botConfig;

    @Value("${bot.webhook-path:#{null}}")
    private String webhookPath;

    @PostMapping("/webhook")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        log.debug("Получено обновление от Telegram: {}", update);
        telegramBot.onUpdateReceived(update);
        return null;
    }

    @GetMapping("/webhook/info")
    public ResponseEntity<String> getWebhookInfo() {
        if (webhookPath != null && !webhookPath.isEmpty()) {
            return ResponseEntity.ok("Webhook настроен на путь: " + webhookPath);
        } else {
            return ResponseEntity.ok("Webhook не настроен. Бот работает в режиме Long Polling.");
        }
    }


    @PostMapping("/webhook/set")
    public ResponseEntity<String> setWebhook() {
        if (webhookPath != null && !webhookPath.isEmpty()) {
            try {
                // Для LongPollingBot нельзя использовать метод registerBot с SetWebhook
                // Вместо этого нужно использовать execute напрямую

                // Создаем запрос на установку webhook
                SetWebhook request = new SetWebhook();
                request.setUrl(webhookPath + "/bot/webhook");

                // Выполняем запрос через API Telegram
                // метод execute доступен в TelegramLongPollingBot
                boolean result = telegramBot.execute(request);

                if (result) {
                    return ResponseEntity.ok("Webhook успешно настроен на путь: " + webhookPath + "/bot/webhook");
                } else {
                    return ResponseEntity.badRequest().body("Не удалось установить webhook");
                }
            } catch (Exception e) {
                log.error("Ошибка настройки webhook: {}", e.getMessage(), e);
                return ResponseEntity.badRequest().body("Ошибка настройки webhook: " + e.getMessage());
            }
        } else {
            return ResponseEntity.badRequest().body("Webhook URL не настроен в конфигурации приложения");
        }
    }


    @PostMapping("/webhook/remove")
    public ResponseEntity<String> removeWebhook() {
        try {
            // Удаляем webhook
            telegramBot.clearWebhook();
            return ResponseEntity.ok("Webhook успешно удален");
        } catch (Exception e) {
            log.error("Ошибка удаления webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка удаления webhook: " + e.getMessage());
        }
    }
}