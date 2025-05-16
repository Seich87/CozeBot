package com.chatassist.cozetalk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chatassist.cozetalk.service.PaymentService;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {

    private final PaymentService paymentService;

    @PostMapping("/callback")
    public ResponseEntity<String> handlePaymentCallback(@RequestBody PaymentNotification notification) {
        log.info("Получено уведомление о платеже: {}", notification);

        try {
            paymentService.handlePaymentNotification(
                    notification.getObject().getId(),
                    notification.getObject().getStatus()
            );
            return ResponseEntity.ok("Notification received");
        } catch (Exception e) {
            log.error("Ошибка обработки уведомления о платеже: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error processing notification: " + e.getMessage());
        }
    }

    @Data
    private static class PaymentNotification {
        private String event;
        private PaymentObject object;
    }

    @Data
    private static class PaymentObject {
        private String id;
        private String status;
        @JsonProperty("paid")
        private boolean isPaid;
    }
}