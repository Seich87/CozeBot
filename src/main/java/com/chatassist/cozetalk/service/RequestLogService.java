package com.chatassist.cozetalk.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatassist.cozetalk.domain.RequestLog;
import com.chatassist.cozetalk.domain.User;
import com.chatassist.cozetalk.repository.RequestLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;

    @Transactional(readOnly = true)
    public List<RequestLog> getLogsByUser(User user, int limit) {
        return requestLogRepository.findByUserOrderByRequestTimeDesc(user, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public int countTodayRequests(User user) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return requestLogRepository.countRequestsForUserToday(user, startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public int countTotalRequests() {
        return (int) requestLogRepository.count();
    }

    @Transactional(readOnly = true)
    public int countRequestsInLastHour() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourAgo = now.minusHours(1);

        return requestLogRepository.countRequestsInPeriod(hourAgo, now);
    }

    @Transactional(readOnly = true)
    public int countRequestsInLastDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayAgo = now.minusDays(1);

        return requestLogRepository.countRequestsInPeriod(dayAgo, now);
    }
}