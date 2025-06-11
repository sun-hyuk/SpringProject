package com.dita.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EventDTO {
    private Integer eventId;
    private String adminId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDate endDate;
    private String imageUrl;
    private LocalDate startDate;
    private String status;  // ENUM('종료','진행중') → String 처리
    private String title;
    private LocalDateTime updatedAt;
    private Integer views;
}