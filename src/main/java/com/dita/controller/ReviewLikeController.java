package com.dita.controller;

import com.dita.dto.MemberDTO;
import com.dita.service.ReviewLikeService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/review-like")
public class ReviewLikeController {

    @Autowired
    private ReviewLikeService reviewLikeService;

    @PostMapping("/{reviewId}")
    public Map<String, Object> toggleLike(@PathVariable int reviewId, HttpSession session) {
    	MemberDTO loginMember = (MemberDTO) session.getAttribute("loggedInMember");
        

    	String loginUserId = loginMember.getMemberId();

        boolean liked = reviewLikeService.toggleLike(loginUserId, reviewId);
        int count = reviewLikeService.countByReview(reviewId);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", count);
        return result;
    }
}
