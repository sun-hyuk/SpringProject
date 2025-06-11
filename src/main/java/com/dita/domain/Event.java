package com.dita.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer eventId;

    @Column(length = 50, nullable = false, columnDefinition = "VARCHAR(50) COLLATE euckr_korean_ci")
    private String adminId;

    @Column(nullable = false, columnDefinition = "TEXT COLLATE euckr_korean_ci")
    private String content;

    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 500, columnDefinition = "VARCHAR(500) COLLATE euckr_korean_ci")
    private String imageUrl;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(columnDefinition = "ENUM('종료','진행중') COLLATE euckr_korean_ci")
    private String status;

    @Column(length = 255, nullable = false, columnDefinition = "VARCHAR(255) COLLATE euckr_korean_ci")
    private String title;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer views = 0;

}
