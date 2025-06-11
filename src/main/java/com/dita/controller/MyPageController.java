package com.dita.controller;

import com.dita.domain.Course;
import com.dita.dto.MemberDTO;
import com.dita.dto.ReviewDTO;
import com.dita.dto.RstJjimDTO;
import com.dita.dto.RstVisitedDTO;
import com.dita.service.CourseJjimService;
import com.dita.service.FavoriteService;
import com.dita.service.InquiryService;
import com.dita.service.ReviewService;
import com.dita.service.RstVisitedService;
import com.dita.vo.InquiryVO;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MyPageController {

    private static final Logger logger = LoggerFactory.getLogger(MyPageController.class);

    private final FavoriteService favoriteService;
    private final InquiryService inquiryService;
    
    @Autowired
    private CourseJjimService courseJjimService;
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private RstVisitedService visitedService;

    // 의존성 주입
    public MyPageController(FavoriteService favoriteService, InquiryService inquiryService) {
        this.favoriteService = favoriteService;
        this.inquiryService = inquiryService;
    }

    /**
     * 1) GET /myPage
     *    - 마이페이지(요약 페이지) 렌더링
     *    - 세션에 저장된 로그인 정보가 있으면 그 계정으로 찜 목록과 문의 목록을 조회하여 모델에 담아줌.
     */
    @GetMapping("/myPage")
    public String myPage(HttpSession session, Model model) {
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");

        if (member != null) {
            logger.info(">> [MyPageController] /myPage – 로그인된 회원: {}", member.getMemberId());
            String memberId = member.getMemberId();

            // ① 전체 찜 목록 조회
            List<RstJjimDTO> fullJjimList = favoriteService.getMyFavoriteList(member.getMemberId());

            // ② 최대 3개까지만 subList
            List<RstJjimDTO> top3Jjim =
                    (fullJjimList.size() > 3) ? fullJjimList.subList(0, 3) : fullJjimList;

            model.addAttribute("jjimList", top3Jjim);
            model.addAttribute("jjimTotalCount", fullJjimList.size());

            // ③ 문의 목록 조회 (최대 2개까지)
            List<InquiryVO> fullInquiryList = inquiryService.getInquiriesByMemberId(member.getMemberId());
            List<InquiryVO> top2Inquiries =
                    (fullInquiryList.size() > 2) ? fullInquiryList.subList(0, 2) : fullInquiryList;

            model.addAttribute("inquiryList", top2Inquiries);
            model.addAttribute("inquiryTotalCount", fullInquiryList.size());

            // ④ 문의 통계 정보
            long ongoingInquiryCount = inquiryService.getInquiryCountByMemberIdAndStatus(member.getMemberId(), "대기중");
            long completedInquiryCount = inquiryService.getInquiryCountByMemberIdAndStatus(member.getMemberId(), "완료");
            
            model.addAttribute("ongoingInquiryCount", ongoingInquiryCount);
            model.addAttribute("completedInquiryCount", completedInquiryCount);
            
            // ⑤ 리뷰 목록 조회 (최대 3개까지)
            List<ReviewDTO> fullReviewList = reviewService.getReviewsByMemberId(member.getMemberId());
            List<ReviewDTO> top3Reviews = (fullReviewList.size() > 3) ? fullReviewList.subList(0, 3) : fullReviewList;

            // 리뷰 데이터 로깅으로 확인
            for (ReviewDTO review : top3Reviews) {
                logger.info(">> [MyPageController] 리뷰 데이터 - ID: {}, 평점: {}, 식당명: {}", 
                    review.getReviewId(), review.getRating(), review.getRestaurantName());
            }

            model.addAttribute("reviewList", top3Reviews);
            model.addAttribute("reviewTotalCount", fullReviewList.size());
            
            // ⑥ 찜한 코스 목록 (최대 3개까지)
            List<Course> top3JjimCourses = courseJjimService.getTop3JjimCourses(member.getMemberId());
            
            model.addAttribute("courseList", top3JjimCourses);
            model.addAttribute("courseTotalCount", top3JjimCourses.size());
            
            List<RstVisitedDTO> recentVisitedViews = visitedService.getRecentVisits(memberId);
            // 3) 모델에 추가
            model.addAttribute("recentList", recentVisitedViews);

        } else {
            logger.info(">> [MyPageController] /myPage – 비로그인 상태");
            model.addAttribute("jjimList", List.of());
            model.addAttribute("jjimTotalCount", 0);
            model.addAttribute("inquiryList", List.of());
            model.addAttribute("inquiryTotalCount", 0);
            model.addAttribute("ongoingInquiryCount", 0);
            model.addAttribute("completedInquiryCount", 0);
            model.addAttribute("reviewList", List.of());
            model.addAttribute("reviewTotalCount", 0);
            model.addAttribute("courseJjimList", List.of());
            model.addAttribute("courseJjimCount", 0);
            model.addAttribute("recentList", 0);
        }

        model.addAttribute("loggedInMember", member);
        return "myPage";
    }

    /**
     * 2) GET /myPage/myInfo
     *    - 내 정보 수정 페이지(프로필 수정) 렌더링
     */
    @GetMapping("/myPage/myInfo")
    public String myInfo(HttpSession session, Model model) {
        // 세션에서 MemberDTO 꺼내기
        MemberDTO member = (MemberDTO) session.getAttribute("loggedInMember");
        if (member == null) {
            // 비로그인 상태라면 로그인 페이지로 redirect
            logger.info(">> [MyPageController] /myPage/myInfo – 비로그인 상태, /login 리다이렉트");
            return "redirect:/login";
        }
        logger.info(">> [MyPageController] /myPage/myInfo – 로그인된 회원: {}", member.getMemberId());

        // 뷰에서 프로필 이미지를 포함한 회원 정보를 표시하기 위해, 모델에 "member"라는 이름으로 추가
        model.addAttribute("member", member);

        // templates/myInfo/myInfo.html 을 렌더링
        return "myInfo/myInfo";
    }
    
    
}