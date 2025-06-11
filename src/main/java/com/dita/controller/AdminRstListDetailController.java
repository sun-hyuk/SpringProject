package com.dita.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dita.dto.RestaurantDTO;
import com.dita.dto.ReviewDTO;
import com.dita.service.RestaurantService;
import com.dita.service.ReviewService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminRstListDetailController {

    private final ReviewService reviewService;

	private final RestaurantService restaurantService;

    @Autowired
    public AdminRstListDetailController(RestaurantService restaurantService, ReviewService reviewService) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
    }
	
	@GetMapping("/adminRstListDetail")
	public String adminRstListDetailPage(Model model,
            @RequestParam(required = false) Integer rst_id,
            @RequestParam(defaultValue = "1") int page) {

		RestaurantDTO restaurant = restaurantService.findRestaurantDetailById(rst_id != null ? rst_id : 1);

	    if (restaurant == null) {
	        // 예외 처리나 에러 페이지 이동
	        return "error/404";
	    }

	    model.addAttribute("restaurant", restaurant);

	    // 리뷰는 아직 없다면 빈 리스트로 넘김
	    List<ReviewDTO> reviews = reviewService.getAllReviewsByRstId(rst_id);
	    model.addAttribute("reviews", reviews);

	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", 0);
	    model.addAttribute("startPage", 1);
	    model.addAttribute("endPage", 1);
	    model.addAttribute("pageSize", 8);
	    model.addAttribute("isRestaurantManagement", true);

	    return "admin/adminRstListDetail";
	}
}