package com.dita.persistence;

import com.dita.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // ✅ [1] 식당 기준 리뷰 정렬
    List<Review> findByRestaurant_RstIdOrderByCreatedAtDesc(Integer rstId);
    List<Review> findByRestaurant_RstIdOrderByRatingDesc(Integer rstId);
    List<Review> findByRestaurant_RstIdOrderByRatingAsc(Integer rstId);

    // ✅ [2] 식당 기준 리뷰 페이징 (ReviewService에서 정렬 파라미터로 활용)
    Page<Review> findByRestaurant_RstId(Integer rstId, Pageable pageable);

    // ✅ [3] 마이페이지 - 회원 기준 리뷰 정렬
    List<Review> findByMember_MemberIdOrderByCreatedAtDesc(String memberId);
    List<Review> findByMember_MemberIdOrderByRatingDesc(String memberId);
    List<Review> findByMember_MemberIdOrderByRatingAsc(String memberId);

    // ✅ [4] 리뷰 평점별 개수
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.restaurant.rstId = :rstId GROUP BY r.rating")
    List<Object[]> countReviewsByRating(@Param("rstId") Integer rstId);

    // ✅ [5] 평균 평점 계산용 (service에서 직접 연산)
    List<Review> findByRestaurant_RstId(Integer rstId);
    
 // member.memberId 기준으로 리뷰 개수 조회
    long countByMemberMemberId(String memberId);
    long countByRestaurant_RstId(Integer rstId);
}
