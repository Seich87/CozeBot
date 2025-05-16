package com.chatassist.cozetalk.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "request_logs")
@Data
@NoArgsConstructor
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime requestTime;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String requestText;

    @Column(columnDefinition = "TEXT")
    private String responseText;

    @Column(nullable = false)
    private String status;

    private Integer processTime;  // Время обработки в миллисекундах

    @PrePersist
    public void prePersist() {
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }
    }
}