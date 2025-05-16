package com.chatassist.cozetalk.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatassist.cozetalk.domain.Subscription;
import com.chatassist.cozetalk.domain.User;
import com.chatassist.cozetalk.domain.enums.TariffPlan;
import com.chatassist.cozetalk.repository.SubscriptionRepository;
import com.chatassist.cozetalk.repository.UserRepository;
import com.chatassist.cozetalk.repository.RequestLogRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final RequestLogRepository requestLogRepository;

    @Transactional(readOnly = true)
    public boolean canUserMakeRequest(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Subscription subscription = user.getSubscription();

        // Проверка наличия активной подписки
        if (subscription == null || subscription.getEndDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Проверка дневного лимита
        int usedToday = getRequestsUsedToday(user);
        return usedToday < subscription.getDailyLimit();
    }

    @Transactional(readOnly = true)
    public int getRemainingRequests(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Subscription subscription = user.getSubscription();
        if (subscription == null) {
            return 0;
        }

        int usedToday = getRequestsUsedToday(user);
        return Math.max(0, subscription.getDailyLimit() - usedToday);
    }

    @Transactional
    public void decrementRemainingRequests(Long telegramId) {
        // Для статистики мы не уменьшаем счетчик напрямую,
        // вместо этого логируем запрос в RequestLog
        // и при следующей проверке будет учтено актуальное количество запросов
    }

    @Transactional
    public void activateSubscription(Long telegramId, TariffPlan tariffPlan) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        Subscription subscription = findByUser(user).orElse(new Subscription());
        LocalDateTime now = LocalDateTime.now();

        if (subscription.getId() == null) {
            subscription.setUser(user);
            subscription.setStartDate(now);
        } else {
            // Если существующая подписка не истекла, продлеваем её
            if (subscription.getEndDate().isAfter(now)) {
                subscription.setEndDate(subscription.getEndDate().plusMonths(1));
            } else {
                subscription.setStartDate(now);
                subscription.setEndDate(now.plusMonths(1));
            }
        }

        subscription.setTariffPlan(tariffPlan);
        subscription.setDailyLimit(tariffPlan.getDailyLimit());
        subscription.setRemainingRequests(30 * tariffPlan.getDailyLimit()); // Примерно на месяц

        subscriptionRepository.save(subscription);
        log.info("Активирована подписка {} для пользователя с ID {}", tariffPlan, telegramId);
    }

    @Transactional(readOnly = true)
    public Optional<Subscription> findByUser(User user) {
        return subscriptionRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Subscription> getAllActiveSubscriptions() {
        return subscriptionRepository.findAllActive(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public long countActiveSubscriptions() {
        return subscriptionRepository.countActiveSubscriptions(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public int getRequestsUsedToday(User user) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return requestLogRepository.countRequestsForUserToday(user, startOfDay, endOfDay);
    }
}