package com.dita.persistence;

import com.dita.domain.Search;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SearchRepository extends JpaRepository<Search, Integer> {

    // member_id로 최근 10건 조회 (삭제되지 않은 것만, 중복 제거)
    @Query(value = """
        SELECT s1.* FROM searchs s1
        INNER JOIN (
            SELECT keyword, MAX(search_at) as latest_search_at
            FROM searchs 
            WHERE member_id = :memberId AND deleted = false
            GROUP BY keyword
        ) s2 ON s1.keyword = s2.keyword AND s1.search_at = s2.latest_search_at
        WHERE s1.member_id = :memberId AND s1.deleted = false
        ORDER BY s1.search_at DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Search> findTop10ByMemberIdAndDeletedFalseOrderBySearchAtDesc(String memberId);
    
    // 전체 검색어 조회 (테스트용)
    List<Search> findAllByOrderBySearchAtDesc();
    
    /**
     * 실시간 인기 검색어 TOP 10
     * 최근 24시간 내 검색된 키워드를 빈도순으로 정렬
     * 개인의 삭제 여부와 관계없이 모든 검색 데이터로 통계 계산
     */
    @Query(value = """
        SELECT keyword, COUNT(*) as search_count 
        FROM searchs 
        WHERE search_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
        GROUP BY keyword 
        ORDER BY search_count DESC, MAX(search_at) DESC 
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTrendingKeywordsRaw();

    /**
     * 특정 회원의 특정 키워드 검색 기록을 논리 삭제 (deleted = true)
     * 실제 데이터는 유지하여 실시간 검색어 통계에는 영향 없음
     */
    @Modifying
    @Transactional
    @Query("UPDATE Search s SET s.deleted = true WHERE s.memberId = :memberId AND s.keyword = :keyword AND s.deleted = false")
    void softDeleteByMemberIdAndKeyword(String memberId, String keyword);
}