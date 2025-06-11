package com.dita.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.dita.domain.ReportStatus;
import com.dita.domain.Report;
import com.dita.domain.ReportType;
import com.dita.dto.ReportListDTO;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    // 1. 전체 신고 리스트를 ReportListDTO로 페이징 조회
    @Query("""
        SELECT new com.dita.dto.ReportListDTO(
            r.reportId,
            r.status,
            r.type,
            r.reporterMember.nickname,
            r.reportedMember.nickname,
            r.reportedAt
        )
        FROM Report r
        ORDER BY r.reportId DESC
    """)
    Page<ReportListDTO> findReportList(Pageable pageable);

    // 2. 필터 적용된 신고 리스트 조회 (DTO)
    @Query("""
        SELECT new com.dita.dto.ReportListDTO(
            r.reportId,
            r.status,
            r.type,
            r.reporterMember.nickname,
            r.reportedMember.nickname,
            r.reportedAt
        )
        FROM Report r
        WHERE (:status IS NULL OR r.status = :status)
          AND (:type IS NULL OR r.type = :type)
          AND (:keyword IS NULL OR :keyword = '' OR r.reporterMember.nickname LIKE %:keyword%)
        ORDER BY r.reportId DESC
    """)
    Page<ReportListDTO> findFilteredReportDTOs(
        @Param("status") ReportStatus status,
        @Param("type") ReportType type,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // 3. 필터 적용된 신고 리스트 조회 (엔티티 반환)
    @Query("""
        SELECT r FROM Report r
        WHERE (:status IS NULL OR r.status = :status)
          AND (:type IS NULL OR r.type = :type)
          AND (:keyword IS NULL OR :keyword = '' OR r.reporterMember.nickname LIKE %:keyword%)
        ORDER BY r.reportId DESC
    """)
    Page<Report> findFilteredReports(
        @Param("status") ReportStatus status,
        @Param("type") ReportType type,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    // 4. 특정 신고자(memberId)의 신고 리스트 조회
    @Query("""
        SELECT new com.dita.dto.ReportListDTO(
            r.reportId,
            r.status,
            r.type,
            r.reporterMember.nickname,
            r.reportedMember.nickname,
            r.reportedAt
        )
        FROM Report r
        WHERE r.reporterMember.memberId = :memberId
        ORDER BY r.reportedAt DESC
    """)
    List<ReportListDTO> findAllByReporterId(@Param("memberId") String memberId);
    
    // 대기 중인 신고 개수
    long countByStatus(ReportStatus status);

    // 대기 중인 신고 중 최신 등록 건
    Optional<Report> findTopByStatusOrderByReportedAtDesc(ReportStatus status);
}
