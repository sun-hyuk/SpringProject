package com.dita.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.dita.dto.MemberDTO;
import com.dita.dto.ReviewDTO;
import com.dita.service.ReviewService;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

@Controller
public class MyReviewController {

	@Autowired
	private ReviewService reviewService;
	
	@GetMapping("/myReview")
	public String myReviewPage(HttpSession session, Model model) {
	    MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
	    if (member == null) {
	        return "redirect:/login?redirectURL=/myReview";
	    }
	    List<ReviewDTO> reviews = reviewService.getReviewsByMemberId(member.getMemberId());
	    model.addAttribute("reviewList", reviews);
	    return "/myReview"; // thymeleaf 뷰 경로
	}
}