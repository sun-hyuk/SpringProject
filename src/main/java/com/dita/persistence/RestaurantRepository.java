package com.dita.persistence;

import com.dita.domain.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;  // 추가
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer>,
                JpaSpecificationExecutor<Restaurant> {  // ← JpaSpecificationExecutor 추가

    // 기존 예시 메서드 (필요하다면 유지)
    List<Restaurant> findByNameContaining(String keyword);
    List<Restaurant> findByStatus(String status);

    // 기존 페이징 가능한 검색 메서드
    Page<Restaurant> findByNameContaining(String keyword, Pageable pageable);
    
    // createdAt 컬럼 기준 내림차순으로 정렬 후, 상위 4개만 조회
    List<Restaurant> findTop4ByOrderByCreatedAtDesc();
    
    // 추가: 페이징 가능한 “최신 등록순” 전체 조회
    Page<Restaurant> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    List<Restaurant> findByMemberIdAndStatus(String memberId, String status);
    
	// 이름으로 레스토랑 검색
	List<Restaurant> findByNameContainingIgnoreCase(String name);
	List<Restaurant> findByTagContainingIgnoreCase(String tag);
    // 별도의 메서드를 더 만들지 않아도, Service에서 Specification을 이용해
    // findAll(spec, pageable) 을 호출하면 동적쿼리 구현이 가능해집니다.

	List<Restaurant> findByNameIn(List<String> names);
	
	// 대기 중인 가게 개수
    long countByStatus(String status);

    // 대기 중인 가게 중 최신 등록 건
    Optional<Restaurant> findTopByStatusOrderByCreatedAtDesc(String status);
	
	@Modifying
    @Query("UPDATE Restaurant r SET r.jjimCount = r.jjimCount + 1 WHERE r.rstId = :rstId")
    void incrementJjimCount(@Param("rstId") Integer rstId);

    @Modifying
    @Query("UPDATE Restaurant r SET r.jjimCount = r.jjimCount - 1 WHERE r.rstId = :rstId")
    void decrementJjimCount(@Param("rstId") Integer rstId);
}
