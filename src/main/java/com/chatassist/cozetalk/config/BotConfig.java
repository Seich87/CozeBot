package com.chatassist.cozetalk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.chatassist.cozetalk.bot.TelegramBot;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class BotConfig {

    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.webhook-path:#{null}}")
    private String webhookPath;

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot bot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

        // Регистрация бота с WebHook или без него в зависимости от настроек
        if (webhookPath != null && !webhookPath.isEmpty()) {
            // Настройка для WebHook (будет настроено через контроллер WebhookController)
            botsApi.registerBot(bot);
        } else {
            // Настройка LongPolling
            botsApi.registerBot(bot);
        }

        return botsApi;
    }
}