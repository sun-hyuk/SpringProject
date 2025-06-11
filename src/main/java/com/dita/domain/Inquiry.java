package com.dita.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Integer inquiryId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private InquiryType type;
    
    @Column(name = "title", nullable = false, length = 50)
    private String title;
    
    @Column(name = "content", nullable = false, length = 500)
    private String content;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InquiryStatus status = InquiryStatus.대기중;
    
    // JPA 관계 매핑 없이 단순 컬럼으로 처리
    @Column(name = "member_id", length = 50)
    private String memberId;
    
    // 열거형 정의
    public enum InquiryType {
        계정문의, 가계문의, 기타
    }
    
    public enum InquiryStatus {
        대기중, 완료
    }
}