package com.dita.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private int reviewId;

    @Column(length = 500, nullable = false, columnDefinition = "VARCHAR(500) COLLATE euckr_korean_ci")
    private String content;

    @Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "menu", nullable = false, length = 255, columnDefinition = "VARCHAR(255) COLLATE euckr_korean_ci")
    private String menu;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(nullable = false)
    private int likes = 0;

    @Column(name = "member_id", length = 50, nullable = true, columnDefinition = "VARCHAR(50) COLLATE euckr_korean_ci")
    private String memberId;

    @Column(name = "rst_id", nullable = true)
    private Integer rstId;

    // 연관 관계 설정 (단방향)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", insertable = false, updatable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rst_id", referencedColumnName = "rst_id", insertable = false, updatable = false)
    private Restaurant restaurant;
    
    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY)
    private List<ReviewImage> reviewImages;
}
