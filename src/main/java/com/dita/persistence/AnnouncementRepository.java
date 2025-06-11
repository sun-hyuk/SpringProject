package com.dita.persistence;

import com.dita.domain.Announcement;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // ────────────────────────────────────────
    // 1) 유형별 페이징 조회
    // ────────────────────────────────────────
    @Query(
      value = "SELECT * " +
              "FROM announcement " +
              "WHERE announcement_type = :type " +
              "ORDER BY created_at DESC",
      countQuery = "SELECT COUNT(*) FROM announcement WHERE announcement_type = :type",
      nativeQuery = true
    )
    Page<Announcement> findByAnnouncementTypeOrderByCreatedAtDesc(
        @Param("type") String type,
        Pageable pageable
    );

    // ────────────────────────────────────────
    // 2) 제목 OR 내용에 키워드 포함 페이징 조회
    //    → 반드시 CONCAT('%', :kw, '%') 형태로 작성해야 합니다.
    // ────────────────────────────────────────
    @Query(
      value = "SELECT * " +
              "FROM announcement " +
              "WHERE title LIKE CONCAT('%', :kw, '%') " +
              "   OR content LIKE CONCAT('%', :kw, '%') " +
              "ORDER BY created_at DESC",
      countQuery = "SELECT COUNT(*) FROM announcement " +
                   "WHERE title LIKE CONCAT('%', :kw, '%') " +
                   "   OR content LIKE CONCAT('%', :kw, '%')",
      nativeQuery = true
    )
    Page<Announcement> findByTitleOrContentLike(
        @Param("kw") String keyword,
        Pageable pageable
    );

    // ────────────────────────────────────────
    // 3) 특정 유형 AND (제목 OR 내용) 포함 페이징 조회
    // ────────────────────────────────────────
    @Query(
      value = "SELECT * " +
              "FROM announcement " +
              "WHERE announcement_type = :type " +
              "  AND (title LIKE CONCAT('%', :kw, '%') " +
              "       OR content LIKE CONCAT('%', :kw, '%')) " +
              "ORDER BY created_at DESC",
      countQuery = "SELECT COUNT(*) FROM announcement " +
                   "WHERE announcement_type = :type " +
                   "  AND (title LIKE CONCAT('%', :kw, '%') " +
                   "       OR content LIKE CONCAT('%', :kw, '%'))",
      nativeQuery = true
    )
    Page<Announcement> findByTypeAndTitleOrContentLike(
        @Param("type") String type,
        @Param("kw")   String keyword,
        Pageable pageable
    );

    // ────────────────────────────────────────
    // 4) 현재 최대 announcement_id 조회
    // ────────────────────────────────────────
    @Query("SELECT COALESCE(MAX(a.announcementId), 0) FROM Announcement a")
    Long findMaxAnnouncementId();

    // ────────────────────────────────────────
    // 5) 조회수 증가
    // ────────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Announcement a SET a.views = a.views + 1 WHERE a.announcementId = :id")
    void increaseViewCount(@Param("id") Long id);
    
    
 // 최신순으로 모든 공지 가져오기
    List<Announcement> findAllByOrderByCreatedAtDesc();
}
