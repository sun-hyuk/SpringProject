package com.dita.persistence;

import com.dita.domain.ReviewLike;
import com.dita.domain.Member;
import com.dita.domain.Review;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Integer> {
    boolean existsByMemberAndReview(Member member, Review review);
    int countByReview(Review review);
    void deleteByMemberAndReview(Member member, Review review);
}
