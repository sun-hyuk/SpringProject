package com.dita.controller;

import com.dita.dto.CourseDTO;
import com.dita.dto.MemberDTO;
import com.dita.dto.RestaurantDTO;
import com.dita.persistence.RestaurantRepository;
import com.dita.service.CourseJjimService;
import com.dita.service.CourseService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TasteCourseController {

    private final CourseService courseService;
    private final RestaurantRepository restaurantRepository;
    private final CourseJjimService jjimService; // ✅ 찜 서비스 추가

    // 코스 전체 목록 페이지
    @GetMapping("/tasteCourse")
    public String listPage(HttpSession session, Model model) {
        MemberDTO loginUser = (MemberDTO) session.getAttribute("loggedInMember");
        model.addAttribute("loggedInMember", loginUser);
        return "tasteCourse/tasteCourse";
    }

    // 코스 상세 페이지
    @GetMapping("/tasteCourseDetail")
    public String courseDetail(@RequestParam("course") int courseId,
                               HttpSession session,
                               Model model) {

        // 1. 코스 정보 + 식당 리스트 가져오기
        CourseDTO course = courseService.getCourseById(courseId);
        List<RestaurantDTO> restaurants = courseService.getRestaurantsByCourseId(courseId);

        // 2. 로그인 상태 확인
        MemberDTO loginUser = (MemberDTO) session.getAttribute("loggedInMember");
        boolean isJjimmed = false;
        if (loginUser != null) {
            isJjimmed = jjimService.isJjimmed(courseId, loginUser.getMemberId());
        }

        // 3. 모델에 값 전달
        model.addAttribute("course", course);
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("loggedInMember", loginUser);
        model.addAttribute("isJjimmed", isJjimmed);

        return "tasteCourse/tasteCourseDetail";
    }

    // 로그인 유도 시 이전 페이지 저장
    @PostMapping("/savePrevPage")
    @ResponseBody
    public void savePrevPage(@RequestParam("url") String url, HttpSession session) {
        session.setAttribute("prevPage", url);
    }
}
