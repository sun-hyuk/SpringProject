package com.dita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RestaurantDTO {

    private Integer rstId;
    private String name;
    private String status;
    private String intro;
    private String address;
    private String phone;
    private BigDecimal rating;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String tag;
    private String regionLabel;
    private String region2Label;
    private String image;
    private Integer jjimCount;
    private LocalDateTime createdAt;
    private String memberId;
    private int reviewCount;

    // 필요하다면, 편의 생성자나 부가 메서드 추가 가능
}
