package com.chatassist.cozetalk.domain.enums;

import lombok.Getter;

@Getter
public enum TariffPlan {
    ROMANTIC(50, 990),  // Романтик
    ALPHA(150, 1990),   // Альфач
    LOVELACE(Integer.MAX_VALUE, 4990);  // Ловелас

    private final int dailyLimit;
    private final int priceInRubles;

    TariffPlan(int dailyLimit, int priceInRubles) {
        this.dailyLimit = dailyLimit;
        this.priceInRubles = priceInRubles;
    }
}