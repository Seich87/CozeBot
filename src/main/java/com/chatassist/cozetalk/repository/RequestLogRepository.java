package com.chatassist.cozetalk.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chatassist.cozetalk.domain.RequestLog;
import com.chatassist.cozetalk.domain.User;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

    List<RequestLog> findByUser(User user);

    List<RequestLog> findByUserOrderByRequestTimeDesc(User user, Pageable pageable);

    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.user = :user AND r.requestTime BETWEEN :startOfDay AND :endOfDay")
    int countRequestsForUserToday(@Param("user") User user,
                                  @Param("startOfDay") LocalDateTime startOfDay,
                                  @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(r) FROM RequestLog r WHERE r.requestTime BETWEEN :startDate AND :endDate")
    int countRequestsInPeriod(@Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate);
}