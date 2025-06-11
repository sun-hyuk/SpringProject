package com.dita.controller;

import com.dita.dto.MemberDTO;
import com.dita.dto.MemberInfoDTO;
import com.dita.dto.MenuDTO;
import com.dita.dto.RestaurantDTO;
import com.dita.dto.ReviewDTO;
import com.dita.dto.ReviewWriteDTO;
import com.dita.service.MenuService;
import com.dita.service.RestaurantService;
import com.dita.service.ReviewService;



import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/reviews") // 👉 모든 리뷰 관련 요청은 /reviews부터 시작
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private RestaurantService restaurantService;
    
    @Autowired
    private MenuService menuService;

    @GetMapping
    @ResponseBody
    public Page<ReviewDTO> getReviewsByRestaurant(
        @RequestParam("rstId") Integer rstId,
        @RequestParam(value = "sort", defaultValue = "latest") String sort,
        @RequestParam(value = "page", defaultValue = "0") int page,
        HttpServletRequest request 
    ) {
        final int pageSize = 5;

        MemberDTO memberDto = (MemberDTO) request.getSession().getAttribute("loggedInMember");
        String loginUserId = (memberDto != null) ? memberDto.getMemberId() : null;

        return reviewService.getReviewsByRstIdSorted(rstId, sort, page, pageSize, loginUserId);
    }

    /**
     * 리뷰 저장 처리
     */
    @GetMapping("/reviewWrite")
    public String reviewWritePage(@RequestParam("rstId") Integer rstId, Model model) {

        // 식당 정보 조회
        RestaurantDTO dto = restaurantService.getRestaurantById(rstId);
        if (dto == null) {
            return "rstSearch"; // 식당 없을 경우 기본 검색 페이지로 이동
        }

        model.addAttribute("restaurant", dto);

        // 메뉴 목록 추가
        List<MenuDTO> menus = menuService.getMenusByRestaurantId(rstId);
        model.addAttribute("menus", menus);

        return "rstSearch/writeReview";  // 뷰: templates/rstSearch/writeReview.html
    }
    
    
    @PostMapping("/reviewWrite")
    public String saveReview(@ModelAttribute ReviewWriteDTO reviewDto,
                             @RequestParam("photos") List<MultipartFile> photos,
                             HttpSession session) {

        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            return "redirect:/login";
        }

        String memberId = member.getMemberId();  // 현재 로그인한 사용자 ID

        reviewService.saveReview(reviewDto, memberId, photos);

        // 리뷰 작성 후 다시 해당 식당 상세 페이지로 이동
        return "redirect:/rstDetail?rstId=" + reviewDto.getRstId();
    }
}
