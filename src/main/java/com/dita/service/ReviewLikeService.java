package com.dita.service;

import com.dita.domain.Member;
import com.dita.domain.Review;
import com.dita.domain.ReviewLike;
import com.dita.persistence.MemberRepository;
import com.dita.persistence.ReviewLikeRepository;
import com.dita.persistence.ReviewRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewLikeService {

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Transactional
    public boolean toggleLike(String memberId, int reviewId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (member == null || review == null) return false;

        boolean alreadyLiked = reviewLikeRepository.existsByMemberAndReview(member, review);
        if (alreadyLiked) {
            reviewLikeRepository.deleteByMemberAndReview(member, review);
            return false; // 좋아요 취소됨
        } else {
            reviewLikeRepository.save(new ReviewLike(member, review));
            return true; // 좋아요 추가됨
        }
    }

    public int countByReview(int reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return 0;
        return reviewLikeRepository.countByReview(review);
    }

    public boolean isLikedByUser(String memberId, int reviewId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (member == null || review == null) return false;
        return reviewLikeRepository.existsByMemberAndReview(member, review);
    }
}
