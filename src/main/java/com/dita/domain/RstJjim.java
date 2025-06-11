// src/main/java/com/dita/domain/RstJjim.java
package com.dita.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rst_jjim")
@Getter @Setter
public class RstJjim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rst_jjim_id")
    private Integer rstJjimId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Member 테이블과 다대일(ManyToOne) 관계: member_id 컬럼이 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    // Restaurant 테이블과 다대일(ManyToOne) 관계: rst_id 컬럼이 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rst_id", referencedColumnName = "rst_id")
    private Restaurant restaurant;
}
