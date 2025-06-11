package com.dita.dto;

import lombok.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertsDTO {
    private Integer alertsId;
    private Boolean isRead;
    private String type;       // enum 이름 그대로 String 처리
    private Integer targetId;
    private String memberId;   // member의 ID만 따로
    private Date createdAt;
}	
