package com.dita.controller;

import com.dita.dto.RestaurantDTO;
import com.dita.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminRstListController {

    private final RestaurantService restaurantService;

    @Autowired
    public AdminRstListController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/adminRstList")
    public String adminRstListPage(Model model,
                                   @RequestParam(defaultValue = "1") int pageNum,
                                   @RequestParam(required = false) String searchType,
                                   @RequestParam(required = false) String searchKeyword) {

        int pageSize = 9;
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("createdAt").descending());

        // 승인된 가게 목록 검색 + 페이징
        Page<RestaurantDTO> restaurantPage = restaurantService.getApprovedRestaurantsPage(searchType, searchKeyword, pageable);
        List<RestaurantDTO> restaurants = restaurantPage.getContent();
        int totalCount = (int) restaurantPage.getTotalElements();
        int totalPages = restaurantPage.getTotalPages();

        // 페이지네이션 범위 계산
        int startPage = Math.max(1, pageNum - 2);
        int endPage = Math.min(totalPages, pageNum + 2);

        // 모델에 필요한 모든 속성 추가
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("searchType", searchType != null ? searchType : "");
        model.addAttribute("searchKeyword", searchKeyword != null ? searchKeyword : "");
        model.addAttribute("isRestaurantManagement", true);

        return "admin/adminRstList";
    }
    
    @PostMapping("/adminDeleteRestaurant")
    public String deleteRestaurant(@RequestParam("rst_id") int rstId,
                                   @RequestParam("pageNum") int pageNum,
                                   @RequestParam(required = false) String searchType,
                                   @RequestParam(required = false) String searchKeyword,
                                   RedirectAttributes redirectAttributes) {
        restaurantService.deleteById(rstId); // 삭제 실행

        redirectAttributes.addFlashAttribute("deleteSuccess", true);

        // 삭제 후 기존 검색 조건 유지하며 목록으로 돌아감
        return "redirect:/adminRstList?pageNum=" + pageNum +
               (searchType != null ? "&searchType=" + searchType : "") +
               (searchKeyword != null ? "&searchKeyword=" + searchKeyword : "");
    }
}
