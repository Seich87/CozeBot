package com.chatassist.cozetalk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatassist.cozetalk.domain.Payment;
import com.chatassist.cozetalk.domain.User;
import com.chatassist.cozetalk.domain.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    List<Payment> findByUser(User user);

    List<Payment> findByUserOrderByCreatedAtDesc(User user);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByOrderByCreatedAtDesc(Pageable pageable);
}