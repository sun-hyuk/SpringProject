package com.dita.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RstVisitedDTO {

    private Integer visitedId;
    private String memberId;
    private Integer restaurantId;
    private LocalDateTime visitedAt;
    
    // 추가 필드: 식당명과 썸네일 URL
    private String restaurantName;
    private String thumbnailUrl;
    private BigDecimal averageRating;
    private String region;
    private boolean bookmarked;
}