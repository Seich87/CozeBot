package com.chatassist.cozetalk.bot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class InlineKeyboardFactory {

    public InlineKeyboardMarkup createTariffKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Кнопка тарифа "Романтик"
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton romanticButton = new InlineKeyboardButton();
        romanticButton.setText("Романтик - 990 ₽/мес");
        romanticButton.setCallbackData("tariff_ROMANTIC");
        row1.add(romanticButton);

        // Кнопка тарифа "Альфач"
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton alphaButton = new InlineKeyboardButton();
        alphaButton.setText("Альфач - 1990 ₽/мес");
        alphaButton.setCallbackData("tariff_ALPHA");
        row2.add(alphaButton);

        // Кнопка тарифа "Ловелас"
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton loveLaceButton = new InlineKeyboardButton();
        loveLaceButton.setText("Ловелас - 4990 ₽/мес");
        loveLaceButton.setCallbackData("tariff_LOVELACE");
        row3.add(loveLaceButton);

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);

        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }
}