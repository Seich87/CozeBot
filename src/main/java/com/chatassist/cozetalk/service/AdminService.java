package com.chatassist.cozetalk.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatassist.cozetalk.domain.Payment;
import com.chatassist.cozetalk.domain.RequestLog;
import com.chatassist.cozetalk.domain.Subscription;
import com.chatassist.cozetalk.domain.User;
import com.chatassist.cozetalk.domain.enums.PaymentStatus;
import com.chatassist.cozetalk.domain.enums.TariffPlan;
import com.chatassist.cozetalk.repository.PaymentRepository;
import com.chatassist.cozetalk.repository.RequestLogRepository;
import com.chatassist.cozetalk.repository.SubscriptionRepository;
import com.chatassist.cozetalk.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Сервис для административных функций бота.
 * Включает в себя статистику, управление пользователями и подписками,
 * а также мониторинг и уведомление о критических ситуациях.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final RequestLogRepository requestLogRepository;

    @Value("${admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${admin.notification.enabled:false}")
    private boolean notificationsEnabled;

    /**
     * Получение статистики по использованию бота.
     *
     * @return Карта с различными статистическими показателями
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayAgo = now.minusDays(1);
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime monthAgo = now.minusDays(30);

        long totalUsers = userRepository.count();
        long activeSubscriptions = subscriptionRepository.countActiveSubscriptions(now);
        long recentRequests = requestLogRepository.countRequestsInPeriod(dayAgo, now);
        long weeklyRequests = requestLogRepository.countRequestsInPeriod(weekAgo, now);
        long monthlyRequests = requestLogRepository.countRequestsInPeriod(monthAgo, now);
        long successfulPayments = paymentRepository.findByStatus(PaymentStatus.SUCCEEDED).size();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", totalUsers);
        statistics.put("activeSubscriptions", activeSubscriptions);
        statistics.put("recentRequests", recentRequests);
        statistics.put("weeklyRequests", weeklyRequests);
        statistics.put("monthlyRequests", monthlyRequests);
        statistics.put("successfulPayments", successfulPayments);

        return statistics;
    }

    /**
     * Принудительное добавление подписки пользователю (административная функция).
     *
     * @param telegramId ID пользователя в Telegram
     * @param tariffPlan Тарифный план
     * @param durationMonths Продолжительность подписки в месяцах
     * @return true, если подписка успешно добавлена, иначе false
     */
    @Transactional
    public boolean addSubscription(Long telegramId, TariffPlan tariffPlan, int durationMonths) {
        Optional<User> userOpt = userRepository.findByTelegramId(telegramId);

        if (userOpt.isEmpty()) {
            log.warn("Попытка добавить подписку несуществующему пользователю: {}", telegramId);
            return false;
        }

        User user = userOpt.get();
        Subscription subscription = subscriptionRepository.findByUser(user).orElse(new Subscription());

        LocalDateTime now = LocalDateTime.now();

        if (subscription.getId() == null) {
            subscription.setUser(user);
            subscription.setStartDate(now);
            subscription.setEndDate(now.plusMonths(durationMonths));
        } else {
            // Если существующая подписка не истекла, продлеваем её
            if (subscription.getEndDate().isAfter(now)) {
                subscription.setEndDate(subscription.getEndDate().plusMonths(durationMonths));
            } else {
                subscription.setStartDate(now);
                subscription.setEndDate(now.plusMonths(durationMonths));
            }
        }

        subscription.setTariffPlan(tariffPlan);
        subscription.setDailyLimit(tariffPlan.getDailyLimit());
        subscription.setRemainingRequests(durationMonths * 30 * tariffPlan.getDailyLimit()); // Примерно на весь срок

        subscriptionRepository.save(subscription);
        log.info("Админ добавил подписку {} на {} месяцев для пользователя с ID {}",
                tariffPlan, durationMonths, telegramId);

        return true;
    }

    /**
     * Отключение подписки пользователя (административная функция).
     *
     * @param telegramId ID пользователя в Telegram
     * @return true, если подписка успешно отключена, иначе false
     */
    @Transactional
    public boolean deactivateSubscription(Long telegramId) {
        Optional<User> userOpt = userRepository.findByTelegramId(telegramId);

        if (userOpt.isEmpty()) {
            log.warn("Попытка отключить подписку несуществующему пользователю: {}", telegramId);
            return false;
        }

        User user = userOpt.get();
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByUser(user);

        if (subscriptionOpt.isEmpty()) {
            log.warn("Попытка отключить несуществующую подписку для пользователя: {}", telegramId);
            return false;
        }

        Subscription subscription = subscriptionOpt.get();
        subscription.setEndDate(LocalDateTime.now().minusSeconds(1)); // Установка времени окончания в прошлом
        subscriptionRepository.save(subscription);

        log.info("Админ отключил подписку для пользователя с ID {}", telegramId);
        return true;
    }

    /**
     * Получение списка пользователей с истекающими подписками.
     *
     * @param daysThreshold Порог в днях для определения истекающих подписок
     * @return Список пользователей с истекающими подписками
     */
    @Transactional(readOnly = true)
    public List<Subscription> getExpiringSubscriptions(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(daysThreshold);
        LocalDateTime now = LocalDateTime.now();

        return subscriptionRepository.findAll().stream()
                .filter(s -> s.getEndDate().isAfter(now) && s.getEndDate().isBefore(threshold))
                .toList();
    }

    /**
     * Получение подробной информации о пользователе для админ-панели.
     *
     * @param telegramId ID пользователя в Telegram
     * @return Карта с детальной информацией о пользователе
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserDetails(Long telegramId) {
        Optional<User> userOpt = userRepository.findByTelegramId(telegramId);

        if (userOpt.isEmpty()) {
            return Map.of("error", "Пользователь не найден");
        }

        User user = userOpt.get();
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByUser(user);
        List<Payment> payments = paymentRepository.findByUserOrderByCreatedAtDesc(user);
        List<RequestLog> recentLogs = requestLogRepository.findByUserOrderByRequestTimeDesc(user, null);

        Map<String, Object> details = new HashMap<>();
        details.put("user", user);
        subscriptionOpt.ifPresent(s -> details.put("subscription", s));
        details.put("payments", payments);
        details.put("recentLogs", recentLogs);

        return details;
    }

    /**
     * Получение последних ошибок запросов для мониторинга.
     *
     * @param limit Максимальное количество ошибок
     * @return Список логов с ошибками
     */
    @Transactional(readOnly = true)
    public List<RequestLog> getRecentErrors(int limit) {
        return requestLogRepository.findAll().stream()
                .filter(log -> "ERROR".equals(log.getStatus()))
                .sorted((a, b) -> b.getRequestTime().compareTo(a.getRequestTime()))
                .limit(limit)
                .toList();
    }

    /**
     * Планировщик для ежедневной проверки и уведомления об истекающих подписках.
     */
    @Scheduled(cron = "0 0 8 * * ?") // Запуск каждый день в 8:00
    public void checkExpiringSubscriptions() {
        if (!notificationsEnabled) {
            return;
        }

        List<Subscription> expiringSubscriptions = getExpiringSubscriptions(3); // Подписки, истекающие в течение 3 дней

        if (!expiringSubscriptions.isEmpty()) {
            log.info("Обнаружены истекающие подписки: {}", expiringSubscriptions.size());
            // Здесь может быть код для отправки уведомления администратору по email
            // или через другие каналы, например, через самого бота
        }
    }

    /**
     * Планировщик для мониторинга критических ошибок.
     */
    @Scheduled(fixedRate = 1800000) // Запуск каждые 30 минут (1800000 мс)
    public void monitorCriticalErrors() {
        if (!notificationsEnabled) {
            return;
        }

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);

        long recentErrorCount = requestLogRepository.findAll().stream()
                .filter(log -> "ERROR".equals(log.getStatus()) && log.getRequestTime().isAfter(threshold))
                .count();

        // Если более 10 ошибок за последние 30 минут, уведомляем администратора
        if (recentErrorCount > 10) {
            log.warn("Обнаружено большое количество ошибок: {} за последние 30 минут", recentErrorCount);
            // Здесь может быть код для отправки уведомления администратору
        }
    }

    /**
     * Получение общей статистики по тарифам.
     *
     * @return Карта со статистикой по тарифам
     */
    @Transactional(readOnly = true)
    public Map<TariffPlan, Long> getTariffStatistics() {
        Map<TariffPlan, Long> stats = new HashMap<>();

        for (TariffPlan plan : TariffPlan.values()) {
            long count = subscriptionRepository.findAll().stream()
                    .filter(s -> s.getEndDate().isAfter(LocalDateTime.now()) && plan.equals(s.getTariffPlan()))
                    .count();
            stats.put(plan, count);
        }

        return stats;
    }
}