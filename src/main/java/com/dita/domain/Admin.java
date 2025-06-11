package com.dita.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DB 쿼리를 보면 테이블 이름은 `admin`이고,
 * 컬럼명은 정확히 `adminId`, `adminPwd`, `adminNickname` 입니다.
 */
@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "adminId")
@ToString
public class Admin {

    /**
     * 실제 DB 컬럼명이 `adminId` 이므로,
     * @Column(name="adminId") 로 정확히 매핑해 줘야 합니다.
     */
    @Id
    @Column(name = "adminId", length = 50, nullable = false)
    private String adminId;

    /**
     * 실제 DB 컬럼명이 `adminPwd` 이므로,
     * 반드시 이 이름으로 매핑해야 JPA에서 조회가 가능합니다.
     */
    @Column(name = "adminPwd", length = 50, nullable = false)
    private String adminPwd;

    /**
     * 실제 DB 컬럼명이 `adminNickname` 이므로,
     * @Column(name="adminNickname") 으로 지정합니다.
     */
    @Column(name = "adminNickname", length = 50, nullable = false, unique = true)
    private String adminNickname;
}
