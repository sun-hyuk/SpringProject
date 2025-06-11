package com.dita.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor

public class RstJjimDTO {
 private Integer rstJjimId;
 private String memberId;
 private Integer rstId;
 private String restaurantName;      // 식당 이름
 private String restaurantAddress;   // 식당 주소 (원한다면 추가 필드)
 private String restaurantImage;
 
 private BigDecimal restaurantRating;  // 평점
 private String restaurantTag;         // 태그
 
 private LocalDateTime createdAt;

}
