package com.dita.persistence;

import com.dita.domain.Member;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;  
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
	
	// 아이디(이메일) 중복 체크
	boolean existsByMemberId(String memberId);
	
	// 닉네임 중복 체크
    boolean existsByNickname(String nickname);
    
    // 전화번호 중복 체크
    boolean existsByPhone(String phone);
    
    /**
     * memberId 필드와 pwd 필드가 모두 일치하는 레코드를 Optional로 반환
     */
    Optional<Member> findByMemberIdAndPwd(String memberId, String pwd);
    Optional<Member> findByMemberId(String memberId);
    
    // 이름, 휴대폰 번호 찾기 - 아이디 찾기
    Optional<Member> findByNameAndPhone(String name, String phone);
    // 아이디, 휴대폰 번호로 찾기 - 비밀번호 찾기
    Optional<Member> findByMemberIdAndPhone(String memberId, String phone);
    
 // 가입일 범위 조회
    Page<Member> findByCreateAtBetween(LocalDateTime from, LocalDateTime to, Pageable p);

    // 리뷰 작성일 범위로 조회
    @Query("""
        SELECT DISTINCT m
          FROM Member m
          JOIN Review r ON r.member = m
         WHERE r.createdAt BETWEEN :from AND :to
    """)
    Page<Member> findByReviewDateBetween(
        @Param("from") LocalDateTime from,
        @Param("to")   LocalDateTime to,
        Pageable pageable);
    
    Page<Member> findByRoleAndCreateAtBetween(
            String role,
            LocalDateTime from,
            LocalDateTime to,
            Pageable p
        );
    
    /** 가입일 기준 최신 회원 한 명 */
    Optional<Member> findTopByOrderByCreateAtDesc();
    
}


