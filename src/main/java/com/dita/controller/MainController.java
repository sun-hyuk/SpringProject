package com.dita.controller;

import com.dita.dto.RestaurantDTO;
import com.dita.dto.RstVisitedDTO;
import com.dita.dto.MemberDTO;
import com.dita.service.RestaurantService;
import com.dita.service.RstVisitedService;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
	
	private final RstVisitedService visitedService;
	private final RestaurantService restaurantService;

    public MainController(RstVisitedService visitedService, RestaurantService restaurantService) {
    	this.visitedService = visitedService;
        this.restaurantService = restaurantService;
    }
    
    @GetMapping("/main")
    public String mainPage(HttpSession session, Model model) {
        // 1) 로그인 사용자
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        model.addAttribute("loggedInMember", member);

        // 2) 최근 방문한 맛집 (로그인한 경우에만)
        if (member != null) {
            List<RstVisitedDTO> recentVisited = 
                visitedService.getRecentVisits(member.getMemberId());
            model.addAttribute("recentVisited", recentVisited);
        }

        // 3) 신상 가게(최근 등록) 로딩
        Pageable pageable = PageRequest.of(0, 4);
        List<String> emptyList = List.of();
        Page<RestaurantDTO> recentPage = restaurantService.searchWithFilters(
            null, "전체", emptyList, emptyList, "최근 등록", pageable
        );
        model.addAttribute("recentRestaurants", recentPage.getContent());

        return "main";
    }
}