package com.dita.controller;

import com.dita.domain.Search;
import com.dita.dto.MemberDTO;
import com.dita.dto.RestaurantDTO;
import com.dita.persistence.SearchRepository;
import com.dita.service.RestaurantService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchRepository searchRepository;
    private final RestaurantService restaurantService; // 인스턴스 주입

    /** 검색 기록 저장 */
    @PostMapping("/log")
    public void saveSearch(@RequestBody SearchReq dto, HttpSession session) {
        Search s = new Search();
        s.setKeyword(dto.keyword());

        // 세션에서 로그인한 회원 정보 가져오기
        MemberDTO loggedInMember = (MemberDTO) session.getAttribute("loggedInMember");
        
        if (loggedInMember != null) {
            // MemberDTO에서 memberId 가져오기
            String memberId = loggedInMember.getMemberId();
            s.setMemberId(memberId);
        }
        // 비로그인 사용자는 member_id가 null로 저장됨
        
        searchRepository.save(s);
    }

    /** 로그인 회원의 최근 검색어 10건 (삭제되지 않은 것만) */
    @GetMapping("/recent")
    public List<Search> recent(HttpSession session) {
        // 세션에서 로그인한 회원 정보 가져오기
        MemberDTO loggedInMember = (MemberDTO) session.getAttribute("loggedInMember");
        
        if (loggedInMember == null) {
            System.out.println("❌ 비로그인 사용자 - 빈 배열 반환");
            return List.of(); // 비로그인이면 빈 배열
        }
        
        String memberId = loggedInMember.getMemberId();
        System.out.println("✅ 로그인 회원: " + memberId + "의 검색어 조회");
        
        List<Search> userSearches = searchRepository.findTop10ByMemberIdAndDeletedFalseOrderBySearchAtDesc(memberId);
        System.out.println("📋 조회된 검색어 개수: " + userSearches.size());
        
        return userSearches;
    }

    /** 실시간 인기 검색어 10위 */
    @GetMapping("/trending")
    public List<Object[]> getTrendingKeywords() {
        return searchRepository.findTrendingKeywordsRaw();
    }

    /** 검색어 논리 삭제 (개인 기록에서만 숨김, 실시간 검색어 통계에는 영향 없음) */
    @DeleteMapping("/delete")
    public void deleteSearch(@RequestBody DeleteReq dto, HttpSession session) {
        // 세션에서 로그인한 회원 정보 가져오기
        MemberDTO loggedInMember = (MemberDTO) session.getAttribute("loggedInMember");
        
        if (loggedInMember != null) {
            String memberId = loggedInMember.getMemberId();
            // 해당 회원의 특정 키워드 검색 기록을 논리 삭제 (deleted = true)
            searchRepository.softDeleteByMemberIdAndKeyword(memberId, dto.keyword());
            System.out.println("✅ 개인 검색어 삭제 완료: " + dto.keyword() + " (회원: " + memberId + ")");
            System.out.println("ℹ️ 실시간 검색어 통계에는 영향 없음 - 전체 통계는 유지됩니다.");
        }
    }
    
    /**
     * 1) 클라이언트가 /api/search/stores?keyword=XXX 로 요청하면
     *    • keyword(검색어)에 해당하는 식당을 “가게명(name), 태그(tag)” 기준으로 조회
     *    • 동시에 검색어를 로그에도 저장
     *
     *  반환: List<RestaurantDTO>
     */
    @GetMapping("/stores")
    public List<RestaurantDTO> searchStores(
            @RequestParam("keyword") String keyword,
            HttpSession session) {

        // 1) 검색어가 비어 있으면 빈 리스트 반환
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        // 2) (선택) 로그인되어 있으면 검색 로그 저장
        MemberDTO loggedInMember = (MemberDTO) session.getAttribute("loggedInMember");
        Search logEntry = new Search();
        logEntry.setKeyword(keyword);
        if (loggedInMember != null) {
            logEntry.setMemberId(loggedInMember.getMemberId());
        }
        searchRepository.save(logEntry);

        // 3) RestaurantService 쪽에서 “가게명(name), 태그(tag)” 으로 검색 수행
        //    service 내부에 searchWithFilters(...) 에서 키워드를 name/intro/tag 에도 매칭하도록 수정해 두었다고 가정
        //    여기서는 간단히 키워드만 넘겨서 pageSize 식당 전체를 가져올 수 있도록 Pageable.unpaged() 사용
        Pageable pageable = Pageable.unpaged();
        Page<RestaurantDTO> pageResult = restaurantService.searchWithFilters(
                keyword,             // 검색어
                "전체",              // 지역 필터 없음을 의미
                List.of(),           // 소분류 필터 없음
                List.of(),           // 음식 종류 필터 없음
                null,                // 신상 가게 필터 없음
                pageable
        );

        return pageResult.getContent();
    }
    

    /* 요청 바디용 record */
    public record SearchReq(String keyword) {}
    public record DeleteReq(String keyword) {}
}