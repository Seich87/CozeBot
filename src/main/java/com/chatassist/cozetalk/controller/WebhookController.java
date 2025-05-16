package com.chatassist.cozetalk.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.chatassist.cozetalk.bot.TelegramBot;
import com.chatassist.cozetalk.config.BotConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
                // Настраиваем webhook для бота
                telegramBot.setWebhook(webhookPath + "/bot/webhook");
                return ResponseEntity.ok("Webhook успешно настроен на путь: " + webhookPath + "/bot/webhook");
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