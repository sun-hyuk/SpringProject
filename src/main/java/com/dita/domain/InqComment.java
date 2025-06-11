package com.dita.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inq_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InqComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inq_comment_id")
    private Integer inqCommentId;
    
    @Column(name = "comments", nullable = false, length = 500)
    private String comments;
    
    @CreationTimestamp
    @Column(name = "create_at")
    private LocalDateTime createAt;
    
    // 외래키 제약조건을 피하기 위해 nullable = true로 설정
    @Column(name = "adminId", length = 50, nullable = true)
    private String adminId;
    
    @Column(name = "inquiry_id")
    private Integer inquiryId;
}