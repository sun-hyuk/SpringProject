package com.dita.domain;

import java.util.Date;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alerts_id")
    private Integer alertsId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AlertType type;

    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

    // 내부 enum 정의
    public enum AlertType {
        식당, 문의, 신고
    }
}
