package com.chatassist.cozetalk.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chatassist.cozetalk.domain.Subscription;
import com.chatassist.cozetalk.domain.User;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUser(User user);

    @Query("SELECT s FROM Subscription s WHERE s.endDate > :now")
    List<Subscription> findAllActive(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.endDate > :now")
    long countActiveSubscriptions(@Param("now") LocalDateTime now);
}