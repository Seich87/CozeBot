package com.chatassist.cozetalk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.chatassist.cozetalk.domain.User;
import com.chatassist.cozetalk.domain.enums.TariffPlan;
import com.chatassist.cozetalk.service.RequestLogService;
import com.chatassist.cozetalk.service.SubscriptionService;
import com.chatassist.cozetalk.service.UserService;
import com.chatassist.cozetalk.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final RequestLogService requestLogService;

    @GetMapping("")
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("activeSubscriptionsCount", subscriptionService.countActiveSubscriptions());
        model.addAttribute("recentPayments", paymentService.getRecentPayments(10));
        model.addAttribute("totalRequests", requestLogService.countTotalRequests());
        model.addAttribute("requestsLastHour", requestLogService.countRequestsInLastHour());
        model.addAttribute("requestsLastDay", requestLogService.countRequestsInLastDay());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/user/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        User user = userService.findByTelegramId(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("user", user);
        model.addAttribute("subscription", subscriptionService.findByUser(user).orElse(null));
        model.addAttribute("payments", paymentService.getPaymentsByUser(user));
        model.addAttribute("tariffPlans", TariffPlan.values());

        return "admin/user-detail";
    }

    @PostMapping("/user/{id}/tariff")
    public String updateUserTariff(@PathVariable Long id, @RequestParam TariffPlan tariffPlan) {
        subscriptionService.activateSubscription(id, tariffPlan);
        return "redirect:/admin/user/" + id;
    }

    @GetMapping("/subscriptions")
    public String listSubscriptions(Model model) {
        model.addAttribute("subscriptions", subscriptionService.getAllActiveSubscriptions());
        return "admin/subscriptions";
    }

    @GetMapping("/payments")
    public String listPayments(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "admin/payments";
    }
}