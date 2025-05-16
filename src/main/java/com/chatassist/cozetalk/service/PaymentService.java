package com.chatassist.cozetalk.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatassist.cozetalk.client.YooKassaApiClient;
import com.chatassist.cozetalk.domain.Payment;
import com.chatassist.cozetalk.domain.User;
import com.chatassist.cozetalk.domain.dto.Amount;
import com.chatassist.cozetalk.domain.dto.Confirmation;
import com.chatassist.cozetalk.domain.dto.PaymentRequest;
import com.chatassist.cozetalk.domain.dto.PaymentResponse;
import com.chatassist.cozetalk.domain.enums.PaymentStatus;
import com.chatassist.cozetalk.domain.enums.TariffPlan;
import com.chatassist.cozetalk.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final YooKassaApiClient yooKassaApiClient;
    private final SubscriptionService subscriptionService;

    @Transactional
    public Mono<PaymentResponse> createPayment(User user, TariffPlan tariffPlan, String returnUrl) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(new Amount(BigDecimal.valueOf(tariffPlan.getPriceInRubles()), "RUB"));
        request.setCapture(true);
        request.setDescription("Подписка на тариф " + tariffPlan.name());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", user.getTelegramId().toString());
        metadata.put("tariffPlan", tariffPlan.name());
        request.setMetadata(metadata);

        // Настройка URL возврата
        request.setConfirmation(new Confirmation("redirect", returnUrl));

        return yooKassaApiClient.createPayment(request)
                .doOnNext(response -> {
                    // Сохранение записи о платеже
                    Payment payment = new Payment();
                    payment.setUser(user);
                    payment.setPaymentId(response.getId());
                    payment.setAmount(response.getAmount().getValue());
                    payment.setCurrency(response.getAmount().getCurrency());
                    payment.setStatus(PaymentStatus.PENDING);
                    payment.setTariffPlan(tariffPlan);

                    paymentRepository.save(payment);
                    log.info("Создан новый платеж: {}", payment);
                });
    }

    @Transactional
    public void handlePaymentNotification(String paymentId, String status) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Платеж не найден"));

        if ("succeeded".equals(status)) {
            payment.setStatus(PaymentStatus.SUCCEEDED);

            // Активация подписки
            subscriptionService.activateSubscription(
                    payment.getUser().getTelegramId(),
                    payment.getTariffPlan());

            log.info("Платеж успешно завершен: {}", payment);
        } else if ("canceled".equals(status)) {
            payment.setStatus(PaymentStatus.CANCELED);
            log.info("Платеж отменен: {}", payment);
        } else if ("waiting_for_capture".equals(status)) {
            // Дополнительная обработка, если требуется
            log.info("Платеж ожидает подтверждения: {}", payment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Платеж завершился с ошибкой: {}", payment);
        }

        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Payment> getRecentPayments(int count) {
        return paymentRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, count));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByUser(User user) {
        return paymentRepository.findByUserOrderByCreatedAtDesc(user);
    }
}