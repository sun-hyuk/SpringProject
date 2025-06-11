package com.dita.persistence;

import com.dita.domain.InqComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InqCommentRepository extends JpaRepository<InqComment, Integer> {
    
    // 특정 문의에 대한 모든 답변 조회 (시간순 정렬)
    List<InqComment> findByInquiryIdOrderByCreateAtAsc(Integer inquiryId);
    
    // 특정 문의에 대한 답변 개수 조회
    long countByInquiryId(Integer inquiryId);
    
    // 특정 관리자가 작성한 답변 조회
    List<InqComment> findByAdminIdOrderByCreateAtDesc(String adminId);
    
    // 특정 문의의 가장 최근 답변 조회
    Optional<InqComment> findTopByInquiryIdOrderByCreateAtDesc(Integer inquiryId);
    
    // 특정 문의의 첫 번째 답변 조회
    Optional<InqComment> findTopByInquiryIdOrderByCreateAtAsc(Integer inquiryId);
    
    // 특정 관리자의 답변 개수 조회
    long countByAdminId(String adminId);
}