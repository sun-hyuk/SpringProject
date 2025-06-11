package com.dita.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "memberId")
@ToString
public class Member {

    @Id
    @Column(name = "member_id", length = 50, nullable = false)
    private String memberId;

    @Column(name = "pwd", length = 50, nullable = false)
    private String pwd;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "phone", length = 50, nullable = false, unique = true)
    private String phone;

    @Column(name = "nickname", length = 50, nullable = false, unique = true)
    private String nickname;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "role", length = 10)
    private String role;  // "user", "boss", "ban"

    @Column(name = "report_score")
    private Integer reportScore;

    @Column(name = "suspended_until")
    private LocalDate suspendedUntil;
    
    /** 리뷰 수 표시용, DB 컬럼 아님 */
    @Transient
    private long reviewCount;
}
