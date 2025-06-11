package com.dita.persistence;

import com.dita.domain.Inquiry;
import com.dita.domain.Inquiry.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {

	// 전체 문의 목록 조회 (최신순)
	List<Inquiry> findAllByOrderByCreatedAtDesc();

	// 회원 ID로 문의 목록 조회 (최신순)
	List<Inquiry> findByMemberIdOrderByCreatedAtDesc(String memberId);

	// 회원 ID와 상태로 문의 목록 조회
	List<Inquiry> findByMemberIdAndStatusOrderByCreatedAtDesc(String memberId, Inquiry.InquiryStatus status);

	// 상태별 문의 개수 조회
	long countByStatus(Inquiry.InquiryStatus status);

	// 회원별 문의 개수 조회
	long countByMemberId(String memberId);

	// 회원별 상태별 문의 개수 조회
	long countByMemberIdAndStatus(String memberId, Inquiry.InquiryStatus status);

	// 특정 회원의 특정 문의 조회
	@Query("SELECT i FROM Inquiry i WHERE i.inquiryId = :inquiryId AND i.memberId = :memberId")
	Inquiry findByInquiryIdAndMemberId(@Param("inquiryId") Integer inquiryId, @Param("memberId") String memberId);


    // 상태(enum) 기준 최신 1건
    Optional<Inquiry> findTopByStatusOrderByCreatedAtDesc(Inquiry.InquiryStatus status);
}

