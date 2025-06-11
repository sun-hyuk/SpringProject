package com.dita.controller;

import com.dita.dto.MemberDTO;
import com.dita.dto.MenuDTO;
import com.dita.dto.RestaurantDTO;
import com.dita.dto.RstVisitedDTO;
import com.dita.service.MenuService;
import com.dita.service.RestaurantService;
import com.dita.service.ReviewService;
import com.dita.service.RstJjimService;
import com.dita.service.RstVisitedService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

@Controller
public class RestaurantController {

    private final RestaurantService restaurantService;
    
    @Autowired
    private MenuService menuService;

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private RstJjimService rstjjimservice;
    
    @Autowired
    private RstVisitedService visitedService;

    @Autowired
    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }
    

    // ─────────────────────────────────────────────────────────────────────────
    // 1) 홈페이지 → 맛집 검색으로 리다이렉트
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/")
    public String home() {
        return "redirect:/rstSearch";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2) 맛집 검색 & 페이징 처리 (검색어, 페이지, 필터 파라미터)
    //    URL 예시:
    //    /rstSearch
    //    /rstSearch?page=2
    //    /rstSearch?q=서면&page=1
    //    /rstSearch?region=busan&details=해운대,수영&types=한식,양식&newStore=오픈%201개월%20이내&page=0
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/rstSearch")
    public String restaurantSearch(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "details", required = false) String detailsCsv,
            @RequestParam(value = "types", required = false) String typesCsv,
            @RequestParam(value = "new", required = false, name = "new") String newStore,
            Model model,
            HttpSession session) 
    {
        // 2) 콤마로 넘어온 detailsCsv, typesCsv를 List<String>으로 분리
        List<String> detailList = (detailsCsv != null && !detailsCsv.isEmpty())
                ? Arrays.stream(detailsCsv.split(","))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList())
                : List.of();

        List<String> typeList = (typesCsv != null && !typesCsv.isEmpty())
                ? Arrays.stream(typesCsv.split(","))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList())
                : List.of();
               
        // 1) 페이징 크기를 10개로 고정
        final int PAGE_SIZE = 10;
        List<RestaurantDTO> list;
        int totalPages = 0;
        
        // 리뷰 많은 순 정렬일 경우 (전체 조회 후 수동 정렬 + 페이징)
        if ("reviews".equals(sort)) {
            list = restaurantService.searchWithFiltersWithoutPage(
                    keyword,
                    (region == null || region.isEmpty()) ? "전체" : region,
                    detailList,
                    typeList,
                    newStore
            );

            for (RestaurantDTO dto : list) {
                int reviewCount = reviewService.getRatingCounts(dto.getRstId())
                        .values().stream().mapToInt(Integer::intValue).sum();
                dto.setReviewCount(reviewCount);
            }

            list.sort(Comparator.comparingInt(RestaurantDTO::getReviewCount).reversed());

            totalPages = (int) Math.ceil((double) list.size() / PAGE_SIZE);
            int fromIndex = Math.min(page * PAGE_SIZE, list.size());
            int toIndex = Math.min(fromIndex + PAGE_SIZE, list.size());
            list = list.subList(fromIndex, toIndex);

        } else {
        // --- 2.2) 정렬 기준에 따른 Sort 객체 생성 ---
        Sort sortObj;
        switch (sort) {
            case "rating":
                sortObj = Sort.by(Sort.Direction.DESC, "rating");      // DTO 필드명이 rating 이면
                break;
            case "jjim":
                sortObj = Sort.by(Sort.Direction.DESC, "jjimCount");
                break;
            default: // "name" or 기타
                sortObj = Sort.by(Sort.Direction.ASC, "name");
        }

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, sortObj);

        // 3) 서비스에 검색어 + 필터 조건 모두 넘겨서 DB 조회 (Page<RestaurantDTO> 반환)
        Page<RestaurantDTO> pageData = restaurantService.searchWithFilters(
                keyword,
                (region == null || region.isEmpty()) ? "전체" : region,
                detailList,
                typeList,
                newStore,
                pageable
        );
        
        list = new ArrayList<>(pageData.getContent());
        totalPages = pageData.getTotalPages();
        }

        // 찜 카운트 세팅
        list.forEach(dto -> dto.setJjimCount(
            rstjjimservice.countJjim(dto.getRstId())
        ));
        
        for (RestaurantDTO dto : list) {
            int count = rstjjimservice.countJjim(dto.getRstId());
            dto.setJjimCount(count);
        }
        
        model.addAttribute("restaurantList", list);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        // 검색어가 null이면 빈 문자열로 채워줘야 Thymeleaf에서 null 오류가 안 남음
        model.addAttribute("keyword", keyword == null ? "" : keyword);

        // 필터 값들도 모두 null 대신 빈 문자열로 채워서 넘겨주면,
        // Thymeleaf 쪽에서 “${region}” 같은 표현식 사용 시 오류가 생기지 않습니다.
        model.addAttribute("region", region == null ? "전체" : region);
        model.addAttribute("details", detailsCsv == null ? "" : detailsCsv);
        model.addAttribute("types", typesCsv == null ? "" : typesCsv);
        model.addAttribute("newStore", newStore == null ? "" : newStore);
        model.addAttribute("currentSort", sort);

        // ▶ [2] bookmarkedIds 주입
        MemberDTO loginMember = (MemberDTO) session.getAttribute("loggedInMember");
        if (loginMember != null) {
            Set<Integer> bookmarkedIds = rstjjimservice.getJjimsByMember(loginMember.getMemberId())
                .stream()
                .map(rj -> rj.getRestaurant().getRstId())
                .collect(Collectors.toSet());
            model.addAttribute("bookmarkedIds", bookmarkedIds);
        }
        
        return "rstSearch/rstSearch";  // 뷰: src/main/resources/templates/rstSearch/rstSearch.html
    }
    
        

    // ─────────────────────────────────────────────────────────────────────────
    // 3) 식당 상세 페이지 (별칭: /rstDetail?name=xxx)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/rstDetail")
    public String restaurantDetailAlias(
            @RequestParam("rstId") Integer rstId,
            Model model,
            HttpSession session) {
        
        if (rstId == null || rstId <= 0) {
            return "redirect:/rstSearch";
        }
        
        // 1) 세션에서 MemberDTO 꺼내기
        MemberDTO loginMember = (MemberDTO) session.getAttribute("loggedInMember");
        String memberId = (loginMember != null ? loginMember.getMemberId() : null);
        model.addAttribute("loggedInMember", loginMember);
        
        if (memberId != null) {
            // 2) 방문기록 저장
            visitedService.recordVisit(memberId, rstId);
            // 3) 최근 방문 리스트 조회
            List<RstVisitedDTO> recent = visitedService.getRecentVisits(memberId);
            model.addAttribute("recentVisited", recent);
        }
        
        // 4) 식당 정보 로딩
        RestaurantDTO dto = restaurantService.getRestaurantById(rstId);
        if (dto == null) {
            return "redirect:/rstSearch";
        }
        model.addAttribute("restaurant", dto);

        // 5) 메뉴, 찜, 리뷰 로직 (기존 그대로)
        List<MenuDTO> menus = menuService.getMenusByRestaurantId(rstId);
        model.addAttribute("menus", menus);

        boolean isJjim = (memberId != null) && rstjjimservice.isJjim(memberId, rstId);
        model.addAttribute("isJjim", isJjim);

        double avgRating = reviewService.calculateAverageRating(rstId);
        Map<Integer, Integer> reviewCounts = reviewService.getRatingCounts(rstId);
        int totalReviews = reviewCounts.values().stream().mapToInt(Integer::intValue).sum();
        Map<Integer, String> widthMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            int count = reviewCounts.getOrDefault(i, 0);
            String width = totalReviews == 0 
                ? "0%" 
                : String.format("%.1f%%", (count * 100.0 / totalReviews));
            widthMap.put(i, width);
        }
        model.addAttribute("ratingList", List.of(5,4,3,2,1));
        model.addAttribute("averageRating", avgRating);
        model.addAttribute("reviewCounts", reviewCounts);
        model.addAttribute("widthMap", widthMap);
        model.addAttribute("totalReviews", totalReviews);
        

        return "rstSearch/rstDetail";
    }


    // ─────────────────────────────────────────────────────────────────────────
    // 4) 리뷰 작성 페이지
    //    /review-write?rstId={식당ID}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/reviewWrite")
    public String reviewWritePage(@RequestParam("rstId") Integer rstId, Model model, HttpSession session) {
        RestaurantDTO dto = restaurantService.getRestaurantById(rstId);
        
        MemberDTO loginUser = (MemberDTO) session.getAttribute("loggedInMember");
        
        if (loginUser == null) {
            // 로그인 후 돌아갈 주소를 파라미터에 담음
            return "redirect:/login?redirectURL=/reviewWrite?rstId=" + rstId;
        }
        
        if (dto == null) {
            return "rstSearch";
        }	
        model.addAttribute("restaurant", dto);
        
        List<MenuDTO> menus = menuService.getMenusByRestaurantId(rstId);
        model.addAttribute("menus", menus);
        return "rstSearch/writeReview";  // 뷰: src/main/resources/templates/rstSearch/writeReview.html
    }
    
    // 검색 페이지 - 리다이렉트 대신 템플릿 직접 반환으로 수정
    @GetMapping("/search")
    public String searchPage(@RequestParam(value = "q", required = false) String keyword, Model model) {
        // 'search.html'이 존재하는지, 그리고 모델에 데이터가 올바르게 들어가는지 확인
        model.addAttribute("keyword", keyword != null ? keyword : "");  // 검색어 파라미터를 모델에 담아 전달
        return "search"; // 템플릿 이름이 'search.html'이어야 함
    }
    
    @GetMapping("/api/restaurants/search")
    @ResponseBody
    public List<RestaurantDTO> searchRestaurants(@RequestParam("keyword") String keyword) {
        return restaurantService.searchRestaurants(keyword);
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    // 5) 테스트용 엔드포인트
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "맛집 검색 컨트롤러가 정상 작동합니다! ✅";
    }
}
