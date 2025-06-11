package com.dita.persistence;

import com.dita.domain.RstVisited;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RstVisitedRepository extends JpaRepository<RstVisited, Integer> {
	
    @Modifying
    @Query(value =
        "INSERT INTO rst_visited (member_id, restaurant_id, visited_at) " +
        "VALUES (:memberId, :restaurantId, :visitedAt) " +
        "ON DUPLICATE KEY UPDATE visited_at = :visitedAt",
        nativeQuery = true)
    void upsertVisit(
        @Param("memberId") String memberId,
        @Param("restaurantId") Integer restaurantId,
        @Param("visitedAt") LocalDateTime visitedAt
    );
    
    // memberId로 최대 10개, 최근 방문순으로
    List<RstVisited> findTop10ByMemberIdOrderByVisitedAtDesc(String memberId);
}
