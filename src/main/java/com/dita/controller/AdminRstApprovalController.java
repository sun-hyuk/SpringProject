package com.dita.controller;

import com.dita.dto.RestaurantDTO;
import com.dita.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminRstApprovalController {

    private final RestaurantService restaurantService;

    @Autowired
    public AdminRstApprovalController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    // ✅ 승인 대기 가게 리스트 페이지
    @GetMapping("/adminRstApproval")
    public String showPendingRestaurants(Model model,
                                         @RequestParam(defaultValue = "1") int pageNum) {

        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("createdAt").descending());
        Page<RestaurantDTO> restaurantPage = restaurantService.getPendingRestaurantsPage(pageable);

        model.addAttribute("pendingRestaurants", restaurantPage.getContent());
        model.addAttribute("totalCount", restaurantPage.getTotalElements());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("startPage", Math.max(1, pageNum - 2));
        model.addAttribute("endPage", Math.min(restaurantPage.getTotalPages(), pageNum + 2));
        model.addAttribute("totalPages", restaurantPage.getTotalPages());

        return "admin/adminRstApproval";
    }

    // 승인 처리
    @PostMapping("/admin/restaurants/approve")
    public String approveRestaurant(@RequestParam("rst_id") int rstId,
                                    @RequestParam(defaultValue = "1") int pageNum,
                                    RedirectAttributes redirectAttributes) {
        restaurantService.approveRestaurant(rstId);
        redirectAttributes.addFlashAttribute("approveSuccess", true);
        return "redirect:/adminRstApproval?pageNum=" + pageNum;
    }

    // 거절 처리 (삭제)
    @PostMapping("/admin/restaurants/reject")
    public String rejectRestaurant(@RequestParam("rst_id") int rstId,
                                   @RequestParam(defaultValue = "1") int pageNum,
                                   RedirectAttributes redirectAttributes) {
        restaurantService.deleteById(rstId);
        redirectAttributes.addFlashAttribute("rejectSuccess", true);
        return "redirect:/adminRstApproval?pageNum=" + pageNum;
    }
}
